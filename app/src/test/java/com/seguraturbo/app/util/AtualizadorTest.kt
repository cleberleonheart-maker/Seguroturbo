package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    @Test
    fun extraiTagDoRedirecionamento() {
        assertEquals(
            "1.36",
            Atualizador.extrairTag("https://github.com/o/r/releases/tag/v1.36")
        )
    }

    @Test
    fun aceitaBarraNoFinal() {
        assertEquals(
            "1.36",
            Atualizador.extrairTag("https://github.com/o/r/releases/tag/v1.36/")
        )
    }

    @Test
    fun aceitaUrlRelativa() {
        assertEquals(
            "2.0.1",
            Atualizador.extrairTag("/o/r/releases/tag/v2.0.1")
        )
    }

    @Test
    fun rejeitaNuloOuVazio() {
        assertNull(Atualizador.extrairTag(null))
        assertNull(Atualizador.extrairTag(""))
        assertNull(Atualizador.extrairTag("   "))
    }

    @Test
    fun rejeitaSegmentoSemVersao() {
        assertNull(Atualizador.extrairTag("https://github.com/o/r/releases"))
        assertNull(Atualizador.extrairTag("https://github.com/o/r/releases/tag/ultima"))
    }
}
