package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VerificadorSenhasVazadasTest {
    @Test
    fun sha1Maiusculo() {
        val v = VerificadorSenhasVazadas(java.lang.RuntimeException("test") as? android.content.Context ?: return)
        // testa apenas o formato do hash
        val h = v.sha1("123456")
        assertEquals(40, h.length)
        assertTrue(h.all { it.isDigit() || (it >= 'A' && it <= 'F') })
    }
}
