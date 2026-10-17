package com.seguraturbo.app.util

import android.content.Context
import com.seguraturbo.app.model.HistoricoEntrada

/**
 * Guarda o histórico de ações (scans, limpezas, benchmarks, turbo) no próprio
 * dispositivo usando SharedPreferences — sem dependências externas (Room/DataStore).
 */
object HistoricoStore {

    private const val PREFS = "seguraturbo_historico"
    private const val CHAVE = "entradas"
    private const val MAXIMO = 20
    private const val SEP = "\u001F"
    private const val QUEBRA = "\n"

    fun listar(ctx: Context): List<HistoricoEntrada> {
        val bruto = prefs(ctx).getString(CHAVE, "") ?: ""
        if (bruto.isBlank()) return emptyList()
        return bruto.split(QUEBRA).mapNotNull { linha ->
            val partes = linha.split(SEP)
            if (partes.size < 4) return@mapNotNull null
            val tipo = runCatching { HistoricoEntrada.Tipo.valueOf(partes[0]) }
                .getOrDefault(HistoricoEntrada.Tipo.SCAN)
            HistoricoEntrada(
                tipo = tipo,
                data = partes[1].toLongOrNull() ?: 0L,
                titulo = partes[2],
                detalhe = partes[3]
            )
        }.sortedByDescending { it.data }
    }

    fun adicionar(ctx: Context, entrada: HistoricoEntrada) {
        val atual = listar(ctx).toMutableList()
        atual.add(0, entrada)
        val limitada = atual.take(MAXIMO)
        val texto = limitada.joinToString(QUEBRA) {
            listOf(it.tipo.name, it.data.toString(), sanitizar(it.titulo), sanitizar(it.detalhe))
                .joinToString(SEP)
        }
        prefs(ctx).edit().putString(CHAVE, texto).apply()
    }

    fun limpar(ctx: Context) {
        prefs(ctx).edit().remove(CHAVE).apply()
    }

    private fun sanitizar(s: String) = s.replace(SEP, " ").replace(QUEBRA, " ")

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
