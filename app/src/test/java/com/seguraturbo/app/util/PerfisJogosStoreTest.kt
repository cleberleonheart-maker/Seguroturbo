package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PerfisJogosStoreTest {
    @Test
    fun estruturaBasica() {
        // Teste estrutural - valida que as classes existem
        assertEquals("PerfilJogo", com.seguraturbo.app.model.PerfilJogo::class.java.simpleName)
    }
}
