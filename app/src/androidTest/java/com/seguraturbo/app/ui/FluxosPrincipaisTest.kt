package com.seguraturbo.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.seguraturbo.app.R
import com.seguraturbo.app.ui.benchmark.BenchmarkScreen
import com.seguraturbo.app.ui.gameturbo.GameTurboScreen
import com.seguraturbo.app.ui.seguranca.SegurancaScreen
import com.seguraturbo.app.ui.theme.SeguroTurboTheme
import com.seguraturbo.app.util.Atualizador
import com.seguraturbo.app.util.Preferencias
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testes instrumentados (Compose UI) dos fluxos principais.
 *
 * Rodar com um aparelho/emulador conectado:
 *   ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class FluxosPrincipaisTest {

    @get:Rule
    val regra = createAndroidComposeRule<ComponentActivity>()

    private fun texto(id: Int) = regra.activity.getString(id)

    @Test
    fun navegacaoMostraAsTresAbasEPermiteTrocar() {
        val ctx = regra.activity
        // Evita o diálogo de novidades poluindo o teste.
        Preferencias.definirUltimaVersaoVista(ctx, Atualizador.versaoInstalada(ctx))

        regra.setContent {
            SeguroTurboTheme { MainScreen() }
        }

        regra.onNodeWithText(texto(R.string.nav_seguranca)).assertIsDisplayed()
        regra.onNodeWithText(texto(R.string.nav_benchmark)).assertIsDisplayed()

        regra.onNodeWithContentDescription(texto(R.string.nav_game_turbo)).performClick()

        regra.waitUntil(5_000) {
            regra.onAllNodesWithText(texto(R.string.ram_titulo)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun segurancaMostraBotaoDeScan() {
        regra.setContent { SeguroTurboTheme { SegurancaScreen() } }
        regra.onNodeWithText(texto(R.string.sec_scan_button)).assertIsDisplayed()
    }

    @Test
    fun benchmarkMostraTesteDeRedeETela() {
        regra.setContent { SeguroTurboTheme { BenchmarkScreen() } }
        regra.onNodeWithText(texto(R.string.rede_titulo)).assertIsDisplayed()
        regra.onNodeWithText(texto(R.string.toque_titulo)).assertIsDisplayed()
    }
}
