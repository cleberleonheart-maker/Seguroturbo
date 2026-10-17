package com.seguraturbo.app.ui.gameturbo

import android.app.Application
import android.content.Context
import android.os.PowerManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seguraturbo.app.model.Game
import com.seguraturbo.app.model.HistoricoEntrada
import com.seguraturbo.app.util.HistoricoStore
import com.seguraturbo.app.util.JogosDetector
import com.seguraturbo.app.util.Preferencias
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Estado da tela Game Turbo. A lista de jogos vem do dispositivo (real), os
 * jogos otimizados ficam salvos nas preferências e a otimização de bateria é
 * consultada de verdade no sistema.
 */
class GameTurboViewModel(app: Application) : AndroidViewModel(app) {

    var modoJogo by mutableStateOf(false)
    var otimizando by mutableStateOf(false)
    var progresso by mutableStateOf(0)
    var mensagem by mutableStateOf<String?>(null)
    var jogos by mutableStateOf<List<Game>>(emptyList())
    var historico by mutableStateOf<List<HistoricoEntrada>>(emptyList())
    /** true = o app está isento da otimização de bateria do sistema. */
    var bateriaIsenta by mutableStateOf(false)

    init {
        carregarHistorico()
        // A detecção lê ícones de todos os apps: faz fora da thread principal.
        viewModelScope.launch {
            val detectados = withContext(Dispatchers.IO) {
                JogosDetector.detectar(getApplication<Application>())
            }
            val otimizados = Preferencias.jogosOtimizados(getApplication<Application>())
            jogos = detectados.map { it.copy(otimizado = it.pacote in otimizados) }
        }
        atualizarBateria()
    }

    private fun carregarHistorico() {
        historico = HistoricoStore.listar(getApplication<Application>())
    }

    /** Consulta se o app está isento da otimização de bateria (Doze). */
    fun atualizarBateria() {
        bateriaIsenta = try {
            val pm = getApplication<Application>()
                .getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(getApplication<Application>().packageName)
        } catch (e: Exception) {
            false
        }
    }

    /** Recebe o valor real do Switch (evita dessincronizar em toques rápidos). */
    fun definirModoJogo(ativo: Boolean) {
        if (ativo == modoJogo) return
        modoJogo = ativo
        HistoricoStore.adicionar(
            getApplication<Application>(),
            HistoricoEntrada(
                tipo = HistoricoEntrada.Tipo.TURBO,
                data = System.currentTimeMillis(),
                titulo = if (modoJogo) "Modo Jogo ativado" else "Modo Jogo desativado",
                detalhe = "Prioridade de recursos do sistema"
            )
        )
        carregarHistorico()
    }

    fun otimizar() {
        if (otimizando) return
        viewModelScope.launch {
            otimizando = true
            progresso = 0
            mensagem = null
            for (i in 1..100) {
                progresso = i
                delay(12)
            }
            jogos = jogos.map { it.copy(otimizado = true) }
            // Persiste os pacotes otimizados para sobreviver a reinícios.
            Preferencias.adicionarJogosOtimizados(getApplication<Application>(), jogos.map { it.pacote })
            mensagem = "Otimização concluída"
            otimizando = false
            HistoricoStore.adicionar(
                getApplication<Application>(),
                HistoricoEntrada(
                    tipo = HistoricoEntrada.Tipo.TURBO,
                    data = System.currentTimeMillis(),
                    titulo = "Otimização de jogos",
                    detalhe = "${jogos.size} jogo(s) otimizado(s)"
                )
            )
            carregarHistorico()
        }
    }

    fun limparHistorico() {
        HistoricoStore.limpar(getApplication<Application>())
        carregarHistorico()
    }
}
