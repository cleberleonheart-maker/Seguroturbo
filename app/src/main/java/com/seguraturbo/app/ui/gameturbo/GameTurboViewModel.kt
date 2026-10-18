package com.seguraturbo.app.ui.gameturbo

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seguraturbo.app.model.Game
import com.seguraturbo.app.model.HistoricoEntrada
import com.seguraturbo.app.model.PerfilDesempenho
import com.seguraturbo.app.util.GerenciadorRam
import com.seguraturbo.app.util.HistoricoStore
import com.seguraturbo.app.util.JogosDetector
import com.seguraturbo.app.util.ModoConcentracao
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
    /** true = o usuário quer ativar o Não Perturbe junto com o Modo Jogo. */
    var dndAuto by mutableStateOf(false)
    /** true = o app tem acesso para alterar o modo Não Perturbe. */
    var dndAcesso by mutableStateOf(false)
    /** Perfil de desempenho atual do Game Turbo. */
    var perfil by mutableStateOf(PerfilDesempenho.EQUILIBRADO)
    /** Temperatura da bateria em °C (null se o sistema não informar). */
    var temperatura by mutableStateOf<Float?>(null)

    /** Memória do aparelho (total/livre em MB) para o cartão de limpeza de RAM. */
    var memoria by mutableStateOf(GerenciadorRam.Memoria(0L, 0L))

    /** true = o diálogo de limpeza de RAM está aberto. */
    var mostrarLimpezaRam by mutableStateOf(false)

    init {
        perfil = Preferencias.perfilDesempenho(getApplication<Application>())
        atualizarTemperatura()
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
        atualizarDnd()
        atualizarRam()
    }

    private fun carregarHistorico() {
        historico = HistoricoStore.listar(getApplication<Application>())
    }

    /** Atualiza a memória total/livre exibida na limpeza de RAM. */
    fun atualizarRam() {
        memoria = GerenciadorRam.memoria(getApplication<Application>())
    }

    /** Registra no histórico/estado o resultado de uma limpeza de RAM. */
    fun memoriasEncerradas(quantidade: Int) {
        atualizarRam()
        if (quantidade > 0) {
            mensagem = "$quantidade app(s) encerrado(s)"
            HistoricoStore.adicionar(
                getApplication<Application>(),
                HistoricoEntrada(
                    tipo = HistoricoEntrada.Tipo.TURBO,
                    data = System.currentTimeMillis(),
                    titulo = "Limpeza de RAM",
                    detalhe = "$quantidade app(s) em segundo plano encerrado(s)"
                )
            )
            carregarHistorico()
        } else {
            mensagem = null
        }
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

    /** Consulta a preferência e o acesso ao modo Não Perturbe. */
    fun atualizarDnd() {
        val app = getApplication<Application>()
        dndAuto = Preferencias.dndAuto(app)
        dndAcesso = ModoConcentracao.temAcesso(app)
    }

    /** Lê a temperatura atual da bateria (°C) pelo broadcast fixo do sistema. */
    fun atualizarTemperatura() {
        temperatura = try {
            val sticky = getApplication<Application>().registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val decimos = sticky?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
                ?: Int.MIN_VALUE
            if (decimos == Int.MIN_VALUE) null else decimos / 10f
        } catch (e: Exception) {
            null
        }
    }

    /** Liga/desliga o Não Perturbe automático. Sem acesso, abre as configurações. */
    fun definirDndAuto(ativo: Boolean) {
        val app = getApplication<Application>()
        dndAuto = ativo
        Preferencias.definirDndAuto(app, ativo)
        if (ativo && !ModoConcentracao.temAcesso(app)) {
            mensagem = null
            ModoConcentracao.pedirAcesso(app)
        }
    }

    /** Recebe o perfil escolhido e aplica os ajustes correspondentes. */
    fun selecionarPerfil(novo: PerfilDesempenho) {
        if (novo == perfil) return
        val app = getApplication<Application>()
        perfil = novo
        Preferencias.definirPerfilDesempenho(app, novo)
        // O perfil Turbo liga o Não Perturbe automático; os demais desligam.
        definirDndAuto(novo == PerfilDesempenho.TURBO)
        HistoricoStore.adicionar(
            app,
            HistoricoEntrada(
                tipo = HistoricoEntrada.Tipo.TURBO,
                data = System.currentTimeMillis(),
                titulo = "Perfil de desempenho",
                detalhe = nomePerfil(novo)
            )
        )
        carregarHistorico()
    }

    private fun nomePerfil(p: PerfilDesempenho): String = when (p) {
        PerfilDesempenho.ECONOMIA -> "Economia"
        PerfilDesempenho.EQUILIBRADO -> "Equilibrado"
        PerfilDesempenho.TURBO -> "Turbo"
    }

    /** Recebe o valor real do Switch (evita dessincronizar em toques rápidos). */
    fun definirModoJogo(ativo: Boolean) {
        if (ativo == modoJogo) return
        modoJogo = ativo
        if (dndAuto) {
            if (ativo) {
                if (!ModoConcentracao.ativar(getApplication<Application>())) {
                    dndAcesso = false
                    ModoConcentracao.pedirAcesso(getApplication<Application>())
                }
            } else {
                ModoConcentracao.restaurar(getApplication<Application>())
            }
        }
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
