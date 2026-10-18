package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

/** Testes dos cálculos puros do teste de toque. */
class TesteToqueTest {

    @Test
    fun calculaToquesPorSegundo() {
        val r = TesteToque.calcular(
            toques = 40,
            duracaoMs = 10_000L,
            intervalosMs = listOf(250L, 250L),
            maxPontos = 2
        )
        assertEquals(4.0, r.toquesPorSegundo, 0.001)
    }

    @Test
    fun respostaMediaUsaIntervalos() {
        val r = TesteToque.calcular(
            toques = 3,
            duracaoMs = 10_000L,
            intervalosMs = listOf(100L, 200L, 300L),
            maxPontos = 1
        )
        assertEquals(200.0, r.respostaMediaMs, 0.001)
    }

    @Test
    fun semIntervalosRespostaZero() {
        val r = TesteToque.calcular(0, 10_000L, emptyList(), 0)
        assertEquals(0.0, r.respostaMediaMs, 0.001)
        assertEquals(0.0, r.toquesPorSegundo, 0.001)
    }

    @Test
    fun duracaoZeroNaoDividePorZero() {
        val r = TesteToque.calcular(5, 0L, emptyList(), 1)
        assertEquals(0.0, r.toquesPorSegundo, 0.001)
    }
}
