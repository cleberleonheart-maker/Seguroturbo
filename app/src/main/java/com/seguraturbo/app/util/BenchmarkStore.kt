package com.seguraturbo.app.util

import android.content.Context

/**
 * Guarda o histórico de pontuações do benchmark (lista de totais) para o gráfico.
 * Persistido em DataStore (com migração automática das SharedPreferences antigas).
 */
object BenchmarkStore {

    private const val CHAVE = "pontuacoes"
    private const val MAXIMO = 20
    private const val SEP = ","

    /** Retorna as pontuações da mais antiga para a mais recente. */
    fun listar(ctx: Context): List<Int> {
        val bruto = Armazenamento.lerTexto(ctx, CHAVE, "") ?: ""
        if (bruto.isBlank()) return emptyList()
        return bruto.split(SEP).mapNotNull { it.trim().toIntOrNull() }
    }

    fun adicionar(ctx: Context, total: Int) {
        val lista = listar(ctx).toMutableList()
        lista.add(total)
        while (lista.size > MAXIMO) lista.removeAt(0)
        Armazenamento.gravarTexto(ctx, CHAVE, lista.joinToString(SEP))
    }

    fun limpar(ctx: Context) {
        Armazenamento.remover(ctx, CHAVE)
    }
}
