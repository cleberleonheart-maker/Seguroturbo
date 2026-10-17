package com.seguraturbo.app.model

/**
 * Uma entrada do histórico de ações do app (scans, limpezas, benchmarks, turbo).
 * Persistida localmente via SharedPreferences (ver HistoricoStore).
 */
data class HistoricoEntrada(
    val tipo: Tipo,
    val data: Long,
    val titulo: String,
    val detalhe: String
) {
    enum class Tipo { SCAN, LIMPEZA, BENCHMARK, TURBO }
}
