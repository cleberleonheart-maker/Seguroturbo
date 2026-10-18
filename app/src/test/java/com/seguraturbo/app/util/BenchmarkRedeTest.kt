package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Testes dos cálculos puros do teste de rede. */
class BenchmarkRedeTest {

    @Test
    fun converteBytesPorSegundoEmMbps() {
        // 1.000.000 bytes em 1s = 8 Mbps
        assertEquals(8.0, BenchmarkRede.mbps(1_000_000L, 1.0), 0.001)
    }

    @Test
    fun tempoZeroNaoDividePorZero() {
        assertEquals(0.0, BenchmarkRede.mbps(500_000L, 0.0), 0.001)
    }

    @Test
    fun melhorLatenciaEscolheOMenor() {
        assertEquals(42L, BenchmarkRede.melhorLatencia(listOf(120L, 42L, 88L)))
    }

    @Test
    fun semAmostrasDeLatenciaRetornaNulo() {
        assertNull(BenchmarkRede.melhorLatencia(emptyList()))
    }
}
