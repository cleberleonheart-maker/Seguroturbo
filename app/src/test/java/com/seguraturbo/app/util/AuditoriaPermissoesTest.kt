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

    @Test
    fun internetComVariosDadosCriticosERiscoAlto() {
        assertEquals(
            RiscoApp.ALTO,
            AuditoriaPermissoes.risco(
                listOf(GrupoPermissao.SMS, GrupoPermissao.LOCALIZACAO),
                temInternet = true
            )
        )
    }

    @Test
    fun internetComUmDadoCriticoERiscoMedio() {
        assertEquals(
            RiscoApp.MEDIO,
            AuditoriaPermissoes.risco(listOf(GrupoPermissao.MICROFONE), temInternet = true)
        )
    }

    @Test
    fun variosDadosCriticosSemInternetERiscoMedio() {
        assertEquals(
            RiscoApp.MEDIO,
            AuditoriaPermissoes.risco(
                listOf(
                    GrupoPermissao.SMS,
                    GrupoPermissao.LOCALIZACAO,
                    GrupoPermissao.CONTATOS
                ),
                temInternet = false
            )
        )
    }

    @Test
    fun soInternetOuSoDadoLeveERiscoBaixo() {
        assertEquals(RiscoApp.BAIXO, AuditoriaPermissoes.risco(emptyList(), temInternet = true))
        assertEquals(
            RiscoApp.BAIXO,
            AuditoriaPermissoes.risco(listOf(GrupoPermissao.CAMERA), temInternet = false)
        )
    }

    @Test
    fun semInternetESemDadosNaoTemRisco() {
        assertEquals(RiscoApp.NENHUM, AuditoriaPermissoes.risco(emptyList(), temInternet = false))
    }

    @Test
    fun resumoRiscoContaPorNivel() {
        val apps = listOf(
            AppComPermissoes("a", "A", listOf(GrupoPermissao.SMS, GrupoPermissao.LOCALIZACAO), true),
            AppComPermissoes("b", "B", listOf(GrupoPermissao.CAMERA), false)
        )
        val resumo = AuditoriaPermissoes.resumoRisco(apps).toMap()
        assertEquals(1, resumo[RiscoApp.ALTO])
        assertEquals(1, resumo[RiscoApp.BAIXO])
        assertTrue(RiscoApp.MEDIO !in resumo)
    }
}
