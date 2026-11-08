package com.seguraturbo.app.util

import com.seguraturbo.app.model.BenchmarkResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenciaClasseTest {

    @Test
    fun faixaAlto() {
        val f = ReferenciaClasse.faixaPara(700_000)
        assertEquals("Alto", f.classe)
    }

    @Test
    fun faixaTopo() {
        val f = ReferenciaClasse.faixaPara(850_000)
        assertEquals("Topo", f.classe)
    }

    @Test
    fun faixaMuitoBasico() {
        val f = ReferenciaClasse.faixaPara(350_000)
        assertEquals("Muito básico", f.classe)
    }

    @Test
    fun totalRealDeAparelhoTopoNaoCaiEmMuitoBasico() {
        val total = BenchmarkResult(cpu = 250_000, gpu = 300_000, mem = 220_000, ux = 160_000).total
        assertEquals(ReferenciaClasse.TOTAL_MAXIMO, total)
        assertEquals("Topo", ReferenciaClasse.faixaPara(total).classe)
    }

    @Test
    fun totalRealDeAparelhoDeEntradaNaoCaiEmTopo() {
        val total = BenchmarkResult(cpu = 80_000, gpu = 120_000, mem = 60_000, ux = 50_000).total
        assertEquals(ReferenciaClasse.TOTAL_MINIMO, total)
        assertEquals("Muito básico", ReferenciaClasse.faixaPara(total).classe)
    }

    @Test
    fun pontuacaoForaDaEscalaFicaLimitada() {
        assertEquals("Muito básico", ReferenciaClasse.faixaPara(0).classe)
        assertEquals("Topo", ReferenciaClasse.faixaPara(5_000_000).classe)
    }

    @Test
    fun resumoContemClasse() {
        val r = ReferenciaClasse.resumo(620_000)
        assertTrue(r.contains("Médio"))
    }
}
