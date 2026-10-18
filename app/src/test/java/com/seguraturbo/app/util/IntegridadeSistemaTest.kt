package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testes da classificação de integridade do sistema (função pura). */
class IntegridadeSistemaTest {

    private fun fatos(
        suEncontrado: Boolean = false,
        pacotesRootPresentes: List<String> = emptyList(),
        tagsTestKeys: Boolean = false,
        tipoBuild: String = "user",
        selinuxEnforcing: Boolean? = true
    ) = IntegridadeSistema.Fatos(
        suEncontrado = suEncontrado,
        pacotesRootPresentes = pacotesRootPresentes,
        tagsTestKeys = tagsTestKeys,
        tipoBuild = tipoBuild,
        selinuxEnforcing = selinuxEnforcing
    )

    @Test
    fun aparelhoLimpoNaoAcusaNada() {
        val d = IntegridadeSistema.classificar(fatos())
        assertFalse(d.rooteado)
        assertFalse(d.bootloaderSuspeito)
        assertEquals(IntegridadeSistema.Selinux.ENFORCING, d.selinux)
    }

    @Test
    fun binarioSuIndicaRoot() {
        assertTrue(IntegridadeSistema.classificar(fatos(suEncontrado = true)).rooteado)
    }

    @Test
    fun pacoteDeRootInstaladoIndicaRoot() {
        val d = IntegridadeSistema.classificar(
            fatos(pacotesRootPresentes = listOf("com.topjohnwu.magisk"))
        )
        assertTrue(d.rooteado)
    }

    @Test
    fun testKeysIndicaRootEBootloaderSuspeito() {
        val d = IntegridadeSistema.classificar(fatos(tagsTestKeys = true))
        assertTrue(d.rooteado)
        assertTrue(d.bootloaderSuspeito)
    }

    @Test
    fun buildUserdebugEhSuspeitoSemSerRoot() {
        val d = IntegridadeSistema.classificar(fatos(tipoBuild = "userdebug"))
        assertFalse(d.rooteado)
        assertTrue(d.bootloaderSuspeito)
    }

    @Test
    fun selinuxPermissivoEhClassificado() {
        val d = IntegridadeSistema.classificar(fatos(selinuxEnforcing = false))
        assertEquals(IntegridadeSistema.Selinux.PERMISSIVE, d.selinux)
    }

    @Test
    fun selinuxIndisponivelFicaDesconhecido() {
        val d = IntegridadeSistema.classificar(fatos(selinuxEnforcing = null))
        assertEquals(IntegridadeSistema.Selinux.DESCONHECIDO, d.selinux)
    }
}
