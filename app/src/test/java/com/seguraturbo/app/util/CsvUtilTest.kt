package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes do escape de campos CSV compartilhado pelos relatórios.
 */
class CsvUtilTest {

    @Test
    fun campoSimplesFicaEntreAspas() {
        assertEquals("\"abc\"", CsvUtil.campo("abc"))
    }

    @Test
    fun aspasDuplasSaoEscapadas() {
        assertEquals("\"diz \"\"oi\"\"\"", CsvUtil.campo("diz \"oi\""))
    }

    @Test
    fun pontoEVirgulaNaoQuebraAColuna() {
        val campo = CsvUtil.campo("a;b;c")
        assertEquals("\"a;b;c\"", campo)
        assertTrue(campo.startsWith("\"") && campo.endsWith("\""))
    }

    @Test
    fun campoVazioContinuaValido() {
        assertEquals("\"\"", CsvUtil.campo(""))
    }

    @Test
    fun quebraDeLinhaNaoVazaDoCampo() {
        val campo = CsvUtil.campo("linha1\nlinha2")
        assertTrue(campo.contains("\n"))
        assertEquals("\"linha1\nlinha2\"", campo)
    }

    @Test
    fun dataHoraTemFormatoEsperado() {
        val texto = CsvUtil.dataHora(0L)
        assertTrue(texto.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")))
    }

    @Test
    fun dataHoraSegundosTemFormatoEsperado() {
        val texto = CsvUtil.dataHoraSegundos(0L)
        assertTrue(texto.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
    }
}
