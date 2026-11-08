package com.seguraturbo.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Testes da serialização do estado completo dos perfis (usado pelo modo
 * economia automático para devolver tudo como estava).
 */
class EstadoPerfisTest {

    private val original = EstadoPerfis(
        perfilUso = PerfilUso.GAMER,
        perfilDesempenho = PerfilDesempenho.TURBO,
        dndAuto = true,
        travaTela = true,
        silenciarMidia = true,
        lembrete = false,
        atualizacaoAuto = true
    )

    @Test
    fun idaEVolta() {
        val lido = EstadoPerfis.fromJson(original.toJson())
        assertEquals(original, lido)
    }

    @Test
    fun jsonVazioDevolveNulo() {
        assertNull(EstadoPerfis.fromJson(null))
        assertNull(EstadoPerfis.fromJson(""))
        assertNull(EstadoPerfis.fromJson("   "))
    }

    @Test
    fun jsonInvalidoDevolveNulo() {
        assertNull(EstadoPerfis.fromJson("isto nao e json"))
    }

    @Test
    fun perfilDesconhecidoUsaPadrao() {
        val json = """{"perfilUso":"NAO_EXISTE","perfilDesempenho":"TAMPouco"}"""
        val lido = EstadoPerfis.fromJson(json)
        assertNotNull(lido)
        assertEquals(PerfilUso.PADRAO, lido!!.perfilUso)
        assertEquals(PerfilDesempenho.EQUILIBRADO, lido.perfilDesempenho)
    }

    @Test
    fun camposAusentesViramFalso() {
        val lido = EstadoPerfis.fromJson("{}")
        assertNotNull(lido)
        assertEquals(false, lido!!.dndAuto)
        assertEquals(false, lido.travaTela)
        assertEquals(false, lido.silenciarMidia)
        assertEquals(false, lido.lembrete)
        assertEquals(false, lido.atualizacaoAuto)
    }
}
