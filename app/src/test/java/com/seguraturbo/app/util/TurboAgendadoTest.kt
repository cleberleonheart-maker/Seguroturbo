package com.seguraturbo.app.util

import org.junit.Assert.assertTrue
import org.junit.Test

class TurboAgendadoTest {
    @Test
    fun constantesReceiver() {
        assertTrue(TurboAgendadoReceiver.ACAO_LIGAR.isNotEmpty())
        assertTrue(TurboAgendadoReceiver.ACAO_DESLIGAR.isNotEmpty())
    }
}
