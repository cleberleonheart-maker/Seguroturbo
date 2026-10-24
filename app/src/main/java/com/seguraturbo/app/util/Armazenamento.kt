package com.seguraturbo.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.armazenamentoStore: DataStore<Preferences> by
    preferencesDataStore(name = "seguraturbo_datastore")

/**
 * Facade síncrona sobre o DataStore, mantendo a mesma forma de uso que as antigas
 * SharedPreferences. Na primeira execução copia os valores dos três arquivos
 * antigos (preferências, histórico e benchmark) para o DataStore, preservando
 * quem já usava o app. Todo o acesso fica local ao dispositivo.
 */
object Armazenamento {

    private const val CHAVE_MIGRADO = "__migrado_sp"
    private val ARQUIVOS_ANTIGOS = listOf(
        "seguraturbo_prefs",
        "seguraturbo_historico",
        "seguraturbo_bench"
    )

    // ---- Leitura ----

    fun lerBooleano(ctx: Context, chave: String, padrao: Boolean): Boolean = runBlocking {
        val ds = dados(ctx)
        ds[booleanPreferencesKey(chave)] ?: padrao
    }

    fun lerInteiro(ctx: Context, chave: String, padrao: Int): Int = runBlocking {
        val ds = dados(ctx)
        ds[intPreferencesKey(chave)] ?: padrao
    }

    fun lerLongo(ctx: Context, chave: String, padrao: Long): Long = runBlocking {
        val ds = dados(ctx)
        ds[longPreferencesKey(chave)] ?: padrao
    }

    fun lerFloat(ctx: Context, chave: String, padrao: Float): Float = runBlocking {
        val ds = dados(ctx)
        ds[floatPreferencesKey(chave)] ?: padrao
    }

    fun lerTexto(ctx: Context, chave: String, padrao: String?): String? = runBlocking {
        val ds = dados(ctx)
        ds[stringPreferencesKey(chave)] ?: padrao
    }

    fun lerConjunto(ctx: Context, chave: String, padrao: Set<String>): Set<String> = runBlocking {
        val ds = dados(ctx)
        ds[stringSetPreferencesKey(chave)] ?: padrao
    }

    /** Todas as chaves/valores atuais (para backup). */
    fun todos(ctx: Context): Map<String, Any> = runBlocking {
        val mapa = dados(ctx).asMap()
        val res = mutableMapOf<String, Any>()
        for ((k, valor) in mapa) {
            val nome = k.name
            if (nome == CHAVE_MIGRADO) continue
            res[nome] = valor ?: ""
        }
        res
    }

    // ---- Escrita ----

    fun gravarBooleano(ctx: Context, chave: String, valor: Boolean) = runBlocking {
        ctx.armazenamentoStore.edit { it[booleanPreferencesKey(chave)] = valor }
    }

    fun gravarInteiro(ctx: Context, chave: String, valor: Int) = runBlocking {
        ctx.armazenamentoStore.edit { it[intPreferencesKey(chave)] = valor }
    }

    fun gravarLongo(ctx: Context, chave: String, valor: Long) = runBlocking {
        ctx.armazenamentoStore.edit { it[longPreferencesKey(chave)] = valor }
    }

    fun gravarFloat(ctx: Context, chave: String, valor: Float) = runBlocking {
        ctx.armazenamentoStore.edit { it[floatPreferencesKey(chave)] = valor }
    }

    fun gravarTexto(ctx: Context, chave: String, valor: String) = runBlocking {
        ctx.armazenamentoStore.edit { it[stringPreferencesKey(chave)] = valor }
    }

    fun gravarConjunto(ctx: Context, chave: String, valor: Set<String>) = runBlocking {
        ctx.armazenamentoStore.edit { it[stringSetPreferencesKey(chave)] = valor }
    }

    fun remover(ctx: Context, chave: String) = runBlocking {
        ctx.armazenamentoStore.edit { it.remove(stringPreferencesKey(chave)) }
    }

    fun limparTudo(ctx: Context) = runBlocking {
        ctx.armazenamentoStore.edit { it.clear() }
    }

    // ---- Migração ----

    private suspend fun dados(ctx: Context): Preferences {
        val atual = ctx.armazenamentoStore.data.first()
        if (atual[booleanPreferencesKey(CHAVE_MIGRADO)] == true) return atual
        migrar(ctx, atual)
        return ctx.armazenamentoStore.data.first()
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
