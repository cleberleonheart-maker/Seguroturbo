package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes do verificador de senhas vazadas: formato do hash e leitura do
 * dicionário offline (funções puras, sem Android).
 */
class VerificadorSenhasVazadasTest {

    @Test
    fun sha1Maiusculo() {
        val h = VerificadorSenhasVazadas.sha1("123456")
        assertEquals(40, h.length)
        assertTrue(h.all { it.isDigit() || (it >= 'A' && it <= 'F') })
    }

    @Test
    fun sha1DeSenhaConhecida() {
        assertEquals(
            "7C4A8D09CA3762AF61E59520943DC26494F8941B",
            VerificadorSenhasVazadas.sha1("123456")
        )
    }

    @Test
    fun formatoValidoAceitaHashEmHexadecimal() {
        assertTrue(VerificadorSenhasVazadas.formatoValido("A".repeat(40)))
        assertFalse(VerificadorSenhasVazadas.formatoValido("A".repeat(39)))
        assertFalse(VerificadorSenhasVazadas.formatoValido("Z".repeat(40)))
        assertFalse(VerificadorSenhasVazadas.formatoValido(""))
    }

    @Test
    fun dicionarioIgnoraComentariosELinhasVazias() {
        val mapa = VerificadorSenhasVazadas.parseDicionario(
            listOf(
                "# comentario",
                "",
                "   ",
                "ABCDE:$SUFIXO_A"
            )
        )
        assertEquals(1, mapa.size)
        assertTrue(mapa.containsKey("ABCDE"))
    }

    @Test
    fun dicionarioIgnoraLinhasMalformadas() {
        val mapa = VerificadorSenhasVazadas.parseDicionario(
            listOf(
                "ABCDE",                                  // sem sufixo
                "ABCDE:123",                             // sufixo curto
                "ABCD:$SUFIXO_A",                        // prefixo curto
                "FGHIJ:$SUFIXO_A",                       // prefixo não hexadecimal
                "ABCDE:ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ", // não hexadecimal
                "ABCDE:$SUFIXO_A"
            )
        )
        assertEquals(1, mapa.size)
        assertEquals(1, mapa["ABCDE"]?.size)
    }

    @Test
    fun dicionarioAgrupaPorPrefixo() {
        val mapa = VerificadorSenhasVazadas.parseDicionario(
            listOf("ABCDE:$SUFIXO_A", "ABCDE:$SUFIXO_B", "FEDCB:$SUFIXO_A")
        )
        assertEquals(2, mapa.size)
        assertEquals(2, mapa["ABCDE"]?.size)
        assertEquals(1, mapa["FEDCB"]?.size)
    }

    @Test
    fun prefixoESufixoDoHashDeSenhaConhecida() {
        val hash = VerificadorSenhasVazadas.sha1("123456")
        val mapa = VerificadorSenhasVazadas.parseDicionario(
            listOf("${hash.substring(0, 5)}:${hash.substring(5)}")
        )
        assertTrue(mapa.containsKey(hash.substring(0, 5)))
    }

    companion object {
        /** Sufixo válido: 35 caracteres hexadecimais. */
        private const val SUFIXO_A = "0123456789ABCDEF0123456789ABCDEF012"

        /** Outro sufixo válido, com o primeiro dígito diferente. */
        private const val SUFIXO_B = "1123456789ABCDEF0123456789ABCDEF012"
    }
}
