package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

/** Testes do cálculo de uso de memória (função pura). */
class GerenciadorRamTest {

    @Test
    fun percentualUsadoCalculaConsumo() {
        val m = GerenciadorRam.Memoria(totalMb = 4000L, livreMb = 1000L)
        assertEquals(3000L, m.usadaMb)
        assertEquals(75, m.percentualUsado)
    }

    @Test
    fun totalZeroNaoDividePorZero() {
        val m = GerenciadorRam.Memoria(totalMb = 0L, livreMb = 0L)
        assertEquals(0, m.percentualUsado)
    }

    @Test
    fun livreMaiorQueTotalNaoFicaNegativo() {
        val m = GerenciadorRam.Memoria(totalMb = 100L, livreMb = 200L)
        assertEquals(0L, m.usadaMb)
    }

    @Test
    fun memoriaCheiaDaCemPorCento() {
        val m = GerenciadorRam.Memoria(totalMb = 2048L, livreMb = 0L)
        assertEquals(100, m.percentualUsado)
    }
}
