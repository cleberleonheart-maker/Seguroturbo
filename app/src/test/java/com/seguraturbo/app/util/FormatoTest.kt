package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

/** Testes da formatação de tamanhos (funções puras, sem dependência do Android). */
class FormatoTest {

    @Test
    fun zeroBytes() {
        assertEquals("0 B", Formato.tamanho(0))
    }

    @Test
    fun tamanhoNegativoViraZero() {
        assertEquals("0 B", Formato.tamanho(-100))
    }

    @Test
    fun bytesSimples() {
        assertEquals("512 B", Formato.tamanho(512))
    }

    @Test
    fun quilobytes() {
        assertEquals("1 KB", Formato.tamanho(1024))
        assertEquals("10 KB", Formato.tamanho(10 * 1024))
    }

    @Test
    fun megabytesSemCasaDecimalSeInteiro() {
        // 2 MB escritos sem separador de milhar não dependem de locale.
        val texto = Formato.tamanho(2L * 1024 * 1024)
        assertEquals(true, texto.endsWith(" MB"))
    }

    @Test
    fun gigabytes() {
        val texto = Formato.tamanho(1024L * 1024 * 1024)
        assertEquals(true, texto.endsWith(" GB"))
    }
}
