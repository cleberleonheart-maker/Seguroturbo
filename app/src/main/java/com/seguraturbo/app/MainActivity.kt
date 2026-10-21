package com.seguraturbo.app

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.seguraturbo.app.ui.MainScreen
import com.seguraturbo.app.ui.onboarding.OnboardingScreen
import com.seguraturbo.app.ui.splash.SplashScreen
import com.seguraturbo.app.ui.theme.Aparencia
import com.seguraturbo.app.ui.theme.Cores
import com.seguraturbo.app.ui.theme.SeguroTurboTheme
import com.seguraturbo.app.util.Agendador
import com.seguraturbo.app.util.AgendadorAtualizacao
import com.seguraturbo.app.util.AtalhoAcao
import com.seguraturbo.app.util.AtalhoPendente
import com.seguraturbo.app.util.Notificacoes
import com.seguraturbo.app.util.Preferencias
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    /** Aba a abrir (0 = Segurança). Pode ser alterada por atalhos via onNewIntent. */
    private val abaInicial = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Aplica o tema e o tamanho da fonte salvos antes de desenhar.
        val modoTema = Preferencias.temaModo(this)
        Aparencia.temaModo = modoTema
        val sistemaEscuro =
            (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        Cores.aplicarModo(modoTema, sistemaEscuro)
        Aparencia.escalaFonte = Preferencias.escalaFonte(this)

        // Prepara os canais de notificação e mantém os agendamentos coerentes.
        Notificacoes.criarCanal(this)
        if (Preferencias.lembreteAtivo(this)) {
            Agendador.agendar(this)
        } else {
            Agendador.cancelar(this)
        }
        if (Preferencias.atualizacaoAutomatica(this)) {
            Notificacoes.criarCanalAtualizacao(this)
            AgendadorAtualizacao.agendar(this)
        } else {
            AgendadorAtualizacao.cancelar(this)
        }

        abaInicial.value = abaDe(intent)
        AtalhoPendente.acao = AtalhoAcao.de(intent.getStringExtra("acao"))

        setContent {
            SeguroTurboTheme {
                val contexto = this@MainActivity
                var mostrarSplash by rememberSaveable { mutableStateOf(true) }
                var mostrarOnboarding by rememberSaveable {
                    mutableStateOf(!Preferencias.onboardingVisto(contexto))
                }
                LaunchedEffect(Unit) {
                    delay(1600)
                    mostrarSplash = false
                }
                val tela = when {
                    mostrarSplash -> "splash"
                    mostrarOnboarding -> "onboarding"
                    else -> "app"
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Crossfade(targetState = tela, label = "fluxo") { atual ->
                        when (atual) {
                            "splash" -> SplashScreen()
                            "onboarding" -> OnboardingScreen(
                                onConcluir = {
                                    Preferencias.definirOnboardingVisto(contexto, true)
                                    mostrarOnboarding = false
                                }
                            )
                            else -> MainScreen(abaInicial = abaInicial.value)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        abaInicial.value = abaDe(intent)
        AtalhoPendente.acao = AtalhoAcao.de(intent.getStringExtra("acao"))
    }

    /** Converte o extra "tela" dos atalhos na aba correspondente. */
    private fun abaDe(intent: Intent?): Int = when (intent?.getStringExtra("tela")) {
        "game_turbo" -> 1
        "benchmark" -> 2
        else -> 0
    }
}
