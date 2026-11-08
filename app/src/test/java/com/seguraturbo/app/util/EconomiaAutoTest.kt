package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes das regras do modo economia automático (função pura, sem Android).
 */
class EconomiaAutoTest {

    @Test
    fun bateriaBaixaAplicaPerfilBateria() {
        val acao = EconomiaAuto.decidir(
            nivel = 15,
            autoAtiva = false,
            perfilAtual = com.seguraturbo.app.model.PerfilUso.GAMER
        )
        assertEquals(EconomiaAuto.Acao.APLICAR, acao)
    }

    @Test
    fun bateriaBaixaComEconomiaJaAtivaNaoFazNada() {
        val acao = EconomiaAuto.decidir(
            nivel = 10,
            autoAtiva = true,
            perfilAtual = com.seguraturbo.app.model.PerfilUso.BATERIA
        )
        assertEquals(EconomiaAuto.Acao.NADA, acao)
    }

    @Test
    fun bateriaRecuperadaRestauraEstado() {
        val acao = EconomiaAuto.decidir(
            nivel = 80,
            autoAtiva = true,
            perfilAtual = com.seguraturbo.app.model.PerfilUso.BATERIA
        )
        assertEquals(EconomiaAuto.Acao.RESTAURAR, acao)
    }

    @Test
    fun usuarioMudouPerfilDuranteEconomiaDescarta() {
        val acao = EconomiaAuto.decidir(
            nivel = 80,
            autoAtiva = true,
            perfilAtual = com.seguraturbo.app.model.PerfilUso.CASUAL
        )
        assertEquals(EconomiaAuto.Acao.DESCARTAR, acao)
    }

    @Test
    fun nivelDesconhecidoNaoFazNada() {
        val acao = EconomiaAuto.decidir(
            nivel = 0,
            autoAtiva = false,
            perfilAtual = com.seguraturbo.app.model.PerfilUso.PADRAO
        )
        assertEquals(EconomiaAuto.Acao.NADA, acao)
    }

    @Test
    fun bateriaBoaSemEconomiaAtivaNaoFazNada() {
        val acao = EconomiaAuto.decidir(
            nivel = 90,
            autoAtiva = false,
            perfilAtual = com.seguraturbo.app.model.PerfilUso.GAMER
        )
        assertEquals(EconomiaAuto.Acao.NADA, acao)
    }

    @Test
    fun limiteBaixoEhVintePorCento() {
        assertEquals(20, EconomiaAuto.LIMITE_BAIXO)
        assertEquals(
            EconomiaAuto.Acao.APLICAR,
            EconomiaAuto.decidir(20, false, com.seguraturbo.app.model.PerfilUso.GAMER)
        )
        assertEquals(
            EconomiaAuto.Acao.NADA,
            EconomiaAuto.decidir(21, false, com.seguraturbo.app.model.PerfilUso.GAMER)
        )
    }

    @Test
    fun perfilBateriaComBateriaBaixaNaoRepeteAcao() {
        val acao = EconomiaAuto.decidir(
            nivel = 5,
            autoAtiva = false,
            perfilAtual = com.seguraturbo.app.model.PerfilUso.BATERIA
        )
        assertFalse(acao == EconomiaAuto.Acao.APLICAR)
        assertTrue(acao == EconomiaAuto.Acao.NADA)
    }
}
