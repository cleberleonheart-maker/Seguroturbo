package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

/** Testes da escolha do volume ao restaurar o som (função pura). */
class SilenciarMidiaTest {

    @Test
    fun usaOVolumeSalvoQuandoValido() {
        assertEquals(7, SilenciarMidia.volumeAoRestaurar(salvo = 7, maximo = 15))
    }

    @Test
    fun semVolumeSalvoUsaMetadeDoMaximo() {
        assertEquals(7, SilenciarMidia.volumeAoRestaurar(salvo = -1, maximo = 15))
    }

    @Test
    fun volumeSalvoAcimaDoMaximoEhIgnorado() {
        assertEquals(5, SilenciarMidia.volumeAoRestaurar(salvo = 99, maximo = 10))
    }

    @Test
    fun volumeSalvoZeroViraMetadeDoMaximo() {
        assertEquals(5, SilenciarMidia.volumeAoRestaurar(salvo = 0, maximo = 10))
    }

    @Test
    fun nuncaDevolveZeroNemNegativo() {
        assertEquals(1, SilenciarMidia.volumeAoRestaurar(salvo = -1, maximo = 0))
    }
}
