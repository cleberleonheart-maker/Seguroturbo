package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ComparacaoTest {

    @Test
    fun historicoVazio() {
        val r = Comparacao.resumo(emptyList())
        assertNull(r.media)
        assertNull(r.melhor)
        assertNull(r.posicao)
        assertEquals(0, r.total)
    }

    @Test
    fun mediaEMelhorNota() {
        val r = Comparacao.resumo(listOf(100, 200, 300))
        assertEquals(200, r.media)
        assertEquals(300, r.melhor)
    }

    @Test
    fun mediaArredondaParaCima() {
        val r = Comparacao.resumo(listOf(100, 101))
        assertEquals(101, r.media)
    }

    @Test
    fun posicaoDoMelhor() {
        assertEquals(1, Comparacao.resumo(listOf(100, 200, 300), 300).posicao)
    }

    @Test
    fun posicaoComEmpate() {
        assertEquals(1, Comparacao.resumo(listOf(300, 300), 300).posicao)
    }

    @Test
    fun posicaoDaPiorNota() {
        assertEquals(3, Comparacao.resumo(listOf(300, 200, 100), 100).posicao)
    }

    @Test
    fun semNotaAtualNaoTemPosicao() {
        val r = Comparacao.resumo(listOf(300, 200, 100), null)
        assertNull(r.posicao)
    }
}
