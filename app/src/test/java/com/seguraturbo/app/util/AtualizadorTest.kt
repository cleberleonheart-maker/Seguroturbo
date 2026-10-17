package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testes da comparação de versões do verificador de atualizações. */
class AtualizadorTest {

    @Test
    fun versoesIguais() {
        assertEquals(0, Atualizador.comparar("1.9", "1.9"))
    }

    @Test
    fun versaoMaisNova() {
        // Comparação numérica: "1.10" é mais nova que "1.9" (ordem de texto diria o contrário).
        assertTrue(Atualizador.comparar("1.10", "1.9") > 0)
    }

    @Test
    fun versaoMaisAntiga() {
        assertTrue(Atualizador.comparar("1.8", "1.9") < 0)
    }

    @Test
    fun majorDiferente() {
        assertTrue(Atualizador.comparar("2.0", "1.9") > 0)
    }

    @Test
    fun numeroDeBlocosDiferente() {
        assertTrue(Atualizador.comparar("1.9.1", "1.9") > 0)
        assertEquals(0, Atualizador.comparar("1.9.0", "1.9"))
    }

    @Test
    fun sufixoNaoNumericoContaComoZero() {
        assertEquals(0, Atualizador.comparar("1.9", "1.9-beta"))
    }
}
