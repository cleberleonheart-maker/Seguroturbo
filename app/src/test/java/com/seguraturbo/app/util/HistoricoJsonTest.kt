package com.seguraturbo.app.util

import com.seguraturbo.app.model.HistoricoEntrada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoricoJsonTest {

    @Test
    fun idaEVolta() {
        val original = listOf(
            HistoricoEntrada(HistoricoEntrada.Tipo.SCAN, 1000L, "Seguro", "0 alertas"),
            HistoricoEntrada(HistoricoEntrada.Tipo.BENCHMARK, 2000L, "42 pontos", "CPU 1 · GPU 2")
        )
        val json = HistoricoStore.entradasParaJson(original)
        val voltou = HistoricoStore.entradasDeJson(json)

        assertEquals(original, voltou)
    }

    @Test
    fun toleranteADadosIncompletos() {
        val json = org.json.JSONArray().apply {
            put(org.json.JSONObject().put("tipo", "NAO_EXISTE").put("data", 5L))
        }
        val lido = HistoricoStore.entradasDeJson(json)
        assertEquals(1, lido.size)
        assertEquals(HistoricoEntrada.Tipo.SCAN, lido[0].tipo)
        assertEquals(5L, lido[0].data)
    }

    @Test
    fun jsonVazio() {
        assertTrue(HistoricoStore.entradasDeJson(org.json.JSONArray()).isEmpty())
    }
}
