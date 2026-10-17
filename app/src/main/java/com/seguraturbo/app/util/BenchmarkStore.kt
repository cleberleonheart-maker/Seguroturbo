package com.seguraturbo.app.util

import android.content.Context

/**
 * Guarda o histórico de pontuações do benchmark (lista de totais) para o gráfico.
 * Persistido em SharedPreferences.
 */
object BenchmarkStore {

    private const val PREFS = "seguraturbo_bench"
    private const val CHAVE = "pontuacoes"
    private const val MAXIMO = 20
    private const val SEP = ","

    /** Retorna as pontuações da mais antiga para a mais recente. */
    fun listar(ctx: Context): List<Int> {
        val bruto = prefs(ctx).getString(CHAVE, "") ?: ""
        if (bruto.isBlank()) return emptyList()
        return bruto.split(SEP).mapNotNull { it.trim().toIntOrNull() }
    }

    fun adicionar(ctx: Context, total: Int) {
        val lista = listar(ctx).toMutableList()
        lista.add(total)
        while (lista.size > MAXIMO) lista.removeAt(0)
        prefs(ctx).edit().putString(CHAVE, lista.joinToString(SEP)).apply()
    }

    fun limpar(ctx: Context) {
        prefs(ctx).edit().remove(CHAVE).apply()
    }

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
