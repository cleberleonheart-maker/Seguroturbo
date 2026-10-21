package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testes da classificação do perfil de dispositivo (função pura). */
class PerfilDispositivoTest {

    private fun fatos(
        deviceOwner: Boolean = false,
        profileOwner: Boolean = false,
        perfilTrabalho: Boolean = false,
        gerenciado: Boolean = false
    ) = PerfilDispositivo.Fatos(
        deviceOwner = deviceOwner,
        profileOwner = profileOwner,
        perfilTrabalho = perfilTrabalho,
        gerenciado = gerenciado
    )

    @Test
    fun aparelhoPessoalNaoEhGerenciado() {
        val d = PerfilDispositivo.classificar(fatos())
        assertEquals(PerfilDispositivo.Tipo.PESSOAL, d.tipo)
        assertFalse(PerfilDispositivo.ehGerenciado(d))
    }

    @Test
    fun deviceOwnerEhGerenciado() {
        val d = PerfilDispositivo.classificar(fatos(deviceOwner = true))
        assertEquals(PerfilDispositivo.Tipo.GERENCIADO, d.tipo)
        assertTrue(PerfilDispositivo.ehGerenciado(d))
    }

    @Test
    fun profileOwnerEhGerenciado() {
        val d = PerfilDispositivo.classificar(fatos(profileOwner = true))
        assertEquals(PerfilDispositivo.Tipo.GERENCIADO, d.tipo)
    }

    @Test
    fun userManagedEhGerenciado() {
        val d = PerfilDispositivo.classificar(fatos(gerenciado = true))
        assertEquals(PerfilDispositivo.Tipo.GERENCIADO, d.tipo)
    }

    @Test
    fun perfilDeTrabalhoSemOwnerEhPerfilTrabalho() {
        val d = PerfilDispositivo.classificar(fatos(perfilTrabalho = true))
        assertEquals(PerfilDispositivo.Tipo.PERFIL_TRABALHO, d.tipo)
        assertTrue(PerfilDispositivo.ehGerenciado(d))
    }
}
