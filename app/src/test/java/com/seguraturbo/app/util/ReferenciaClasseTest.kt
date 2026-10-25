package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenciaClasseTest {
    @Test
    fun faixaAlto() {
        val f = ReferenciaClasse.faixaPara(70000)
        assertEquals("Alto", f.classe)
    }

    @Test
    fun faixaTopo() {
        val f = ReferenciaClasse.faixaPara(95000)
        assertEquals("Topo", f.classe)
    }

    @Test
    fun resumoContemClasse() {
        val r = ReferenciaClasse.resumo(50000)
        assertTrue(r.contains("Médio"))
    }
}
