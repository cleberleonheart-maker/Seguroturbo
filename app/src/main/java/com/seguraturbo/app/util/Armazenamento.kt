package com.seguraturbo.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

private val Context.armazenamentoStore: DataStore<Preferences> by
    preferencesDataStore(name = "seguraturbo_datastore")

/**
 * Facade sobre o DataStore, mantendo a mesma forma de uso que as antigas
 * SharedPreferences. Na primeira execução copia os valores dos três arquivos
 * antigos (preferências, histórico e benchmark) para o DataStore, preservando
 * quem já usava o app. Todo o acesso fica local ao dispositivo.
 *
 * As leituras são servidas de um cache em memória: só a primeira carga toca o
 * disco. As gravações atualizam o cache na hora (a leitura seguinte já vê o
 * valor novo) e persistem no DataStore em segundo plano, evitando travar a
 * interface a cada toque em um botão.
 */
object Armazenamento {

    private const val CHAVE_MIGRADO = "__migrado_sp"
    private val ARQUIVOS_ANTIGOS = listOf(
        "seguraturbo_prefs",
        "seguraturbo_historico",
        "seguraturbo_bench"
    )

    /**
     * Escritas em uma única thread, em ordem: assim um "remover" seguido de
     * "gravar" (ou o contrário) nunca chega ao disco fora de ordem.
     */
    private val dispatcherEscrita: CoroutineDispatcher =
        Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "seguraturbo-dados") }
            .asCoroutineDispatcher()

    /** Escopo das gravações em segundo plano. */
    private val escopo = CoroutineScope(SupervisorJob() + dispatcherEscrita)

    /** Snapshot em memória dos valores (imutável, substituído a cada escrita). */
    @Volatile
    private var cache: Map<String, Any>? = null

    /** Quantas gravações ainda estão pendentes no disco. */
    private val pendentes = java.util.concurrent.atomic.AtomicInteger(0)

    // ---- Leitura ----

    fun lerBooleano(ctx: Context, chave: String, padrao: Boolean): Boolean =
        valores(ctx)[chave] as? Boolean ?: padrao

    fun lerInteiro(ctx: Context, chave: String, padrao: Int): Int =
        when (val v = valores(ctx)[chave]) {
            is Int -> v
            is Long -> v.toInt()
            is Float -> v.toInt()
            else -> padrao
        }

    fun lerLongo(ctx: Context, chave: String, padrao: Long): Long =
        when (val v = valores(ctx)[chave]) {
            is Long -> v
            is Int -> v.toLong()
            is Float -> v.toLong()
            else -> padrao
        }

    fun lerFloat(ctx: Context, chave: String, padrao: Float): Float =
        when (val v = valores(ctx)[chave]) {
            is Float -> v
            is Double -> v.toFloat()
            is Int -> v.toFloat()
            is Long -> v.toFloat()
            else -> padrao
        }

    fun lerTexto(ctx: Context, chave: String, padrao: String?): String? =
        valores(ctx)[chave] as? String ?: padrao

    fun lerConjunto(ctx: Context, chave: String, padrao: Set<String>): Set<String> {
        val bruto = valores(ctx)[chave] as? Set<*> ?: return padrao
        return bruto.filterIsInstance<String>().toSet()
    }

    /** Todas as chaves/valores atuais (para backup). */
    fun todos(ctx: Context): Map<String, Any> = valores(ctx)

    // ---- Escrita ----

    fun gravarBooleano(ctx: Context, chave: String, valor: Boolean) =
        gravar(ctx, chave, valor) { it[booleanPreferencesKey(chave)] = valor }

    fun gravarInteiro(ctx: Context, chave: String, valor: Int) =
        gravar(ctx, chave, valor) { it[intPreferencesKey(chave)] = valor }

    fun gravarLongo(ctx: Context, chave: String, valor: Long) =
        gravar(ctx, chave, valor) { it[longPreferencesKey(chave)] = valor }

    fun gravarFloat(ctx: Context, chave: String, valor: Float) =
        gravar(ctx, chave, valor) { it[floatPreferencesKey(chave)] = valor }

    fun gravarTexto(ctx: Context, chave: String, valor: String) =
        gravar(ctx, chave, valor) { it[stringPreferencesKey(chave)] = valor }

    fun gravarConjunto(ctx: Context, chave: String, valor: Set<String>) =
        gravar(ctx, chave, valor) { it[stringSetPreferencesKey(chave)] = valor }

    fun remover(ctx: Context, chave: String) =
        gravar(ctx, chave, null) { it.remove(stringPreferencesKey(chave)) }

    /** Apaga tudo, mas mantém a marca de migração (evita reimportar o antigo). */
    fun limparTudo(ctx: Context) {
        synchronized(this) {
            cache = emptyMap()
        }
        pendentes.incrementAndGet()
        escopo.launch {
            try {
                runCatching {
                    ctx.armazenamentoStore.edit {
                        it.clear()
                        it[booleanPreferencesKey(CHAVE_MIGRADO)] = true
                    }
                }
            } finally {
                pendentes.decrementAndGet()
            }
        }
    }

    /**
     * Atualiza o cache na hora e agenda a gravação no DataStore. O cache é
     * copiado a cada escrita (copy-on-write), então quem está lendo nunca
     * enxerga um mapa pela metade.
     */
    private fun gravar(
        ctx: Context,
        chave: String,
        valor: Any?,
        edicao: suspend (MutablePreferences) -> Unit
    ) {
        synchronized(this) {
            val novo = valores(ctx).toMutableMap()
            if (valor == null) novo.remove(chave) else novo[chave] = valor
            cache = novo
        }
        pendentes.incrementAndGet()
        escopo.launch {
            try {
                runCatching { ctx.armazenamentoStore.edit(edicao) }
            } finally {
                pendentes.decrementAndGet()
            }
        }
    }

    /**
     * Aguarda as gravações pendentes. Usado ao sair da activity para garantir
     * que a última alteração do usuário já está no disco.
     */
    suspend fun sincronizar() {
        while (pendentes.get() > 0) {
            delay(20)
        }
    }

    /** Valores em memória, carregando do DataStore na primeira chamada. */
    private fun valores(ctx: Context): Map<String, Any> {
        cache?.let { return it }
        return synchronized(this) {
            cache ?: runBlocking { carregar(ctx) }.also { cache = it }
        }
    }

    // ---- Migração ----

    private suspend fun carregar(ctx: Context): Map<String, Any> {
        var atual = ctx.armazenamentoStore.data.first()
        if (atual[booleanPreferencesKey(CHAVE_MIGRADO)] != true) {
            migrar(ctx, atual)
            atual = ctx.armazenamentoStore.data.first()
        }
        return atual.asMap()
            .filterKeys { it.name != CHAVE_MIGRADO }
            .mapKeys { it.key.name }
    }

    private suspend fun migrar(ctx: Context, atual: Preferences) {
        val antigos = mutableMapOf<String, Any>()
        ARQUIVOS_ANTIGOS.forEach { arquivo ->
            runCatching {
                ctx.getSharedPreferences(arquivo, Context.MODE_PRIVATE).all.forEach { (k, v) ->
                    if (v != null) antigos[k] = v
                }
            }
        }
        ctx.armazenamentoStore.edit { ds ->
            antigos.forEach { (k, v) ->
                when (v) {
                    is Boolean -> ds[booleanPreferencesKey(k)] = v
                    is Int -> ds[intPreferencesKey(k)] = v
                    is Long -> ds[longPreferencesKey(k)] = v
                    is Float -> ds[floatPreferencesKey(k)] = v
                    is String -> ds[stringPreferencesKey(k)] = v
                    is Set<*> -> ds[stringSetPreferencesKey(k)] =
                        v.filterIsInstance<String>().toSet()
                }
            }
            ds[booleanPreferencesKey(CHAVE_MIGRADO)] = true
        }
    }
}
