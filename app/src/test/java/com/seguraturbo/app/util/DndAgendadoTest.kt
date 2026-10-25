package com.seguraturbo.app.util

import org.junit.Assert.assertTrue
import org.junit.Test

class DndAgendadoTest {
    @Test
    fun constantesReceiver() {
        assertTrue(DndAgendadoReceiver.ACAO_LIGAR.isNotEmpty())
        assertTrue(DndAgendadoReceiver.ACAO_DESLIGAR.isNotEmpty())
    }
}
