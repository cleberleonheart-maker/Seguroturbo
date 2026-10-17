package com.seguraturbo.app.util

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testes do cálculo de idade do patch de segurança (função pura de data). */
class SecurityScannerTest {

    @Test
    fun dataAntigaRetornaMesesAcimaDeUmAno() {
        val meses = SecurityScanner.mesesDesde("2020-01-01")
        assertTrue(meses != null && meses > 12)
    }

    @Test
    fun dataInvalidaRetornaNulo() {
        assertNull(SecurityScanner.mesesDesde("nao-e-data"))
    }

    @Test
    fun dataFuturaNaoFicaNegativa() {
        val meses = SecurityScanner.mesesDesde("2999-01-01")
        assertTrue(meses != null && meses >= 0)
    }
}
