package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testes da geração do CSV do inventário (função pura, sem Android). */
class InventarioCsvTest {

    private val rotulo: (GrupoPermissao) -> String = { it.name.lowercase() }

    @Test
    fun cabecalhoPresente() {
        val csv = InventarioCsv.montarCsv(emptyList(), rotulo)
        assertEquals("pacote;nome;permissoes_sensiveis\n", csv)
    }

    @Test
    fun umaLinhaPorAppComGruposSeparadosPorVirgula() {
        val csv = InventarioCsv.montarCsv(
            listOf(
                AppComPermissoes(
                    "com.app.a", "App A",
                    listOf(GrupoPermissao.CAMERA, GrupoPermissao.MICROFONE)
                )
            ),
            rotulo
        )
        assertTrue(csv.contains("\"com.app.a\";\"App A\";\"camera, microfone\""))
    }

    @Test
    fun escapaAspasDuplas() {
        val csv = InventarioCsv.montarCsv(
            listOf(AppComPermissoes("pkg", "App \"X\"", listOf(GrupoPermissao.SMS))),
            rotulo
        )
        assertTrue(csv.contains("\"App \"\"X\"\"\""))
    }

    @Test
    fun ordenaPorNome() {
        val csv = InventarioCsv.montarCsv(
            listOf(
                AppComPermissoes("b", "Zebra", listOf(GrupoPermissao.CAMERA)),
                AppComPermissoes("a", "Abelha", listOf(GrupoPermissao.CAMERA))
            ),
            rotulo
        )
        val linhas = csv.trim().lines()
        assertTrue(linhas[1].contains("Abelha"))
        assertTrue(linhas[2].contains("Zebra"))
    }

    @Test
    fun nomeSugeridoTemPrefixoEData() {
        assertTrue(InventarioCsv.nomeSugerido().startsWith("seguraturbo_inventario_"))
        assertTrue(InventarioCsv.nomeSugerido().endsWith(".csv"))
    }
}
