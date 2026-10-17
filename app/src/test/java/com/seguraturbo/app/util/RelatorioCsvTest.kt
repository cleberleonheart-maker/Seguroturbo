package com.seguraturbo.app.util

import com.seguraturbo.app.model.HistoricoEntrada
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testes da geração do CSV do histórico (função pura, sem Android). */
class RelatorioCsvTest {

    @Test
    fun cabecalhoPresente() {
        val csv = RelatorioCsv.montarCsv(emptyList())
        assertTrue(csv.startsWith("data;tipo;titulo;detalhe"))
    }

    @Test
    fun escapaAspasDuplas() {
        val csv = RelatorioCsv.montarCsv(
            listOf(
                HistoricoEntrada(
                    HistoricoEntrada.Tipo.SCAN, 1_700_000_000_000L,
                    "Titulo \"x\"", "detalhe; com ponto e vírgula"
                )
            )
        )
        assertTrue(csv.contains("\"Titulo \"\"x\"\"\""))
    }

    @Test
    fun maisRecentePrimeiro() {
        val antigo = HistoricoEntrada(HistoricoEntrada.Tipo.SCAN, 1000L, "antigo", "")
        val novo = HistoricoEntrada(HistoricoEntrada.Tipo.SCAN, 2000L, "novo", "")
        val linhas = RelatorioCsv.montarCsv(listOf(antigo, novo)).trim().lines()
        assertTrue(linhas[1].contains("novo"))
    }
}
