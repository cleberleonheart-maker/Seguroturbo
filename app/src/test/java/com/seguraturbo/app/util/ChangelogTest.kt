package com.seguraturbo.app.util

import com.seguraturbo.app.model.Changelog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangelogTest {

    @Test
    fun listaNaoEstaVazia() {
        assertTrue(Changelog.versoes.isNotEmpty())
    }

    @Test
    fun versoesSaoDiferentes() {
        val versoes = Changelog.versoes.map { it.versao }
        assertEquals(versoes.size, versoes.toSet().size)
    }

    @Test
    fun buscaPorVersaoEncontraEIgnoraDesconhecidas() {
        assertNotNull(Changelog.para("1.18"))
        assertNull(Changelog.para("0.0"))
    }
}
