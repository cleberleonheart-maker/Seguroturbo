package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testes do agrupamento de permissões da auditoria. */
class AuditoriaPermissoesTest {

    @Test
    fun cameraMapeadaParaUmGrupo() {
        assertEquals(
            listOf(GrupoPermissao.CAMERA),
            AuditoriaPermissoes.gruposDe(setOf("android.permission.CAMERA"))
        )
    }

    @Test
    fun localizacaoAceitaFinaOuAproximada() {
        assertTrue(
            GrupoPermissao.LOCALIZACAO in
                AuditoriaPermissoes.gruposDe(setOf("android.permission.ACCESS_COARSE_LOCATION"))
        )
        assertTrue(
            GrupoPermissao.LOCALIZACAO in
                AuditoriaPermissoes.gruposDe(setOf("android.permission.ACCESS_FINE_LOCATION"))
        )
    }

    @Test
    fun variasPermissoesViramVariosGrupos() {
        val grupos = AuditoriaPermissoes.gruposDe(
            setOf(
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO",
                "android.permission.READ_SMS"
            )
        )
        assertEquals(
            listOf(GrupoPermissao.CAMERA, GrupoPermissao.MICROFONE, GrupoPermissao.SMS),
            grupos
        )
    }

    @Test
    fun permissaoComumNaoConta() {
        assertTrue(
            AuditoriaPermissoes.gruposDe(setOf("android.permission.VIBRATE")).isEmpty()
        )
    }

    @Test
    fun resumoContaAppsPorGrupo() {
        val apps = listOf(
            AppComPermissoes("a", "App A", listOf(GrupoPermissao.CAMERA, GrupoPermissao.MICROFONE)),
            AppComPermissoes("b", "App B", listOf(GrupoPermissao.CAMERA))
        )
        val resumo = AuditoriaPermissoes.resumo(apps).toMap()
        assertEquals(2, resumo[GrupoPermissao.CAMERA])
        assertEquals(1, resumo[GrupoPermissao.MICROFONE])
        assertTrue(GrupoPermissao.SMS !in resumo)
    }
}
