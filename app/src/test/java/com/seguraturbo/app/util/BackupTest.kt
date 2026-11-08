package com.seguraturbo.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

/**
 * Testes da leitura do arquivo de backup: limite de tamanho e recusa de
 * arquivos inválidos.
 */
class BackupTest {

    @Test
    fun arquivoDentroDoLimiteEhLido() {
        val conteudo = """{"app":"SeguroTurbo","versao":1}"""
        val texto = Backup.lerLimitado(ByteArrayInputStream(conteudo.toByteArray()))
        assertEquals(conteudo, texto)
    }

    @Test
    fun arquivoGrandeDemaisEhRecusado() {
        val grande = ByteArray(Backup.TAMANHO_MAXIMO + 1024) { 'a'.code.toByte() }
        assertNull(Backup.lerLimitado(ByteArrayInputStream(grande)))
    }

    @Test
    fun arquivoVazioDevolveTextoVazio() {
        val texto = Backup.lerLimitado(ByteArrayInputStream(ByteArray(0)))
        assertNotNull(texto)
        assertEquals("", texto)
    }

    @Test
    fun limiteDeTamanhoEDeDoisMegas() {
        assertEquals(2 * 1024 * 1024, Backup.TAMANHO_MAXIMO)
        assertTrue(Backup.TAMANHO_MAXIMO > 0)
    }
}
