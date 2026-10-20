package com.seguraturbo.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testes do detector de stalkerware (lógica pura, sem Android). */
class DetectorStalkerwareTest {

    @Test
    fun listaDePacotesNaoEstaVaziaENaoTemDuplicados() {
        val pacotes = DetectorStalkerware.pacotesConhecidos()
        assertTrue(pacotes.isNotEmpty())
        assertTrue(pacotes.size == pacotes.toSet().size)
    }

    @Test
    fun combinacaoSuspeitaExigeTresSinais() {
        val tres = setOf(
            "android.permission.READ_SMS",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.READ_CONTACTS"
        )
        assertTrue(DetectorStalkerware.combinacaoPerigosaDe(tres))
    }

    @Test
    fun receiveSmsContaComoSinalDeSms() {
        val sinais = setOf(
            "android.permission.RECEIVE_SMS",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.RECORD_AUDIO"
        )
        assertTrue(DetectorStalkerware.combinacaoPerigosaDe(sinais))
    }

    @Test
    fun doisSinaisNaoBastam() {
        val dois = setOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.RECORD_AUDIO"
        )
        assertFalse(DetectorStalkerware.combinacaoPerigosaDe(dois))
    }

    @Test
    fun appComumNaoEDetectado() {
        assertFalse(
            DetectorStalkerware.combinacaoPerigosaDe(setOf("android.permission.CAMERA"))
        )
    }
}
