package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testes da normalização usada para transformar medições em pontuação. */
class BenchmarkEngineTest {

    @Test
    fun valorNoMinimoRetornaSaidaMinima() {
        assertEquals(80_000, BenchmarkEngine.normalizar(20_000, 20_000, 400_000, 80_000, 250_000))
    }

    @Test
    fun valorNoMaximoRetornaSaidaMaxima() {
        assertEquals(250_000, BenchmarkEngine.normalizar(400_000, 20_000, 400_000, 80_000, 250_000))
    }

    @Test
    fun valorAbaixoDoMinimoFicaNoMinimo() {
        assertEquals(80_000, BenchmarkEngine.normalizar(0, 20_000, 400_000, 80_000, 250_000))
    }

    @Test
    fun valorAcimaDoMaximoFicaNoMaximo() {
        assertEquals(250_000, BenchmarkEngine.normalizar(999_999, 20_000, 400_000, 80_000, 250_000))
    }

    @Test
    fun valorNoMeioFicaProximoDoMeioDaFaixa() {
        val meio = BenchmarkEngine.normalizar(210_000, 20_000, 400_000, 80_000, 250_000)
        assertTrue(meio in 160_000..170_000)
    }
}
