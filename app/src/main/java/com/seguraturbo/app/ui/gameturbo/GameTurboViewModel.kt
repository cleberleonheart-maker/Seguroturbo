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
import com.seguraturbo.app.util.BloqueioTela
import com.seguraturbo.app.util.GerenciadorRam
import com.seguraturbo.app.util.HistoricoStore
import com.seguraturbo.app.util.JogosDetector
import com.seguraturbo.app.util.ModoConcentracao
import com.seguraturbo.app.util.Preferencias
import com.seguraturbo.app.util.SilenciarMidia
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
    /** true = fixar a tela (screen pinning) ao abrir um jogo. */
    var travaTelaJogo by mutableStateOf(false)
    /** true = o bloqueio de tela está ligado nas configurações do sistema. */
    var travaTelaSistema by mutableStateOf(true)
    /** true = silenciar só a mídia enquanto o Modo Jogo está ativo. */
    var silenciarMidia by mutableStateOf(false)
    /** true = limpar RAM automaticamente ao ligar o Modo Jogo. */
    var autoLimparRamJogo by mutableStateOf(false)
    /** Sessão de foco Pomodoro ligada ao Não Perturbe. */
    var sessaoFocoAtiva by mutableStateOf(false)
    var sessaoFocoRestanteSeg by mutableStateOf(0L)
    /** Perfil de desempenho atual do Game Turbo. */
    var perfil by mutableStateOf(PerfilDesempenho.EQUILIBRADO)
    /** Temperatura da bateria em °C (null se o sistema não informar). */
    var temperatura by mutableStateOf<Float?>(null)

    // ---- Turbo agendado ----
    var turboAgendadoAtivo by mutableStateOf(false)
    var turboHoraLigar by mutableStateOf(18)
    var turboMinLigar by mutableStateOf(0)
    var turboHoraDesligar by mutableStateOf(23)
    var turboMinDesligar by mutableStateOf(0)

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
        travaTelaJogo = Preferencias.travaTelaJogo(getApplication<Application>())
        atualizarTravaTela()
        silenciarMidia = Preferencias.silenciarMidia(getApplication<Application>())
        autoLimparRamJogo = Preferencias.autoLimparRamJogo(getApplication<Application>())
        carregarSessaoFoco()
        carregarTurboAgendado()
    }

    private fun carregarTurboAgendado() {
        val app = getApplication<Application>()
        turboAgendadoAtivo = Preferencias.turboAgendadoAtivo(app)
        turboHoraLigar = Preferencias.turboAgendadoHoraLigar(app)
        turboMinLigar = Preferencias.turboAgendadoMinLigar(app)
        turboHoraDesligar = Preferencias.turboAgendadoHoraDesligar(app)
        turboMinDesligar = Preferencias.turboAgendadoMinDesligar(app)
    }

    fun definirTurboAgendadoAtivo(ativo: Boolean) {
        turboAgendadoAtivo = ativo
        Preferencias.definirTurboAgendadoAtivo(getApplication<Application>(), ativo)
    }

    fun definirTurboHoraLigar(hora: Int, minuto: Int) {
        turboHoraLigar = hora
        turboMinLigar = minuto
        val app = getApplication<Application>()
        Preferencias.definirTurboAgendadoHoraLigar(app, hora)
        Preferencias.definirTurboAgendadoMinLigar(app, minuto)
    }

    fun definirTurboHoraDesligar(hora: Int, minuto: Int) {
        turboHoraDesligar = hora
        turboMinDesligar = minuto
        val app = getApplication<Application>()
        Preferencias.definirTurboAgendadoHoraDesligar(app, hora)
        Preferencias.definirTurboAgendadoMinDesligar(app, minuto)
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

    /** Consulta se o screen pinning está habilitado nas configurações. */
    fun atualizarTravaTela() {
        travaTelaSistema = BloqueioTela.ativadoNoSistema(getApplication<Application>())
    }

    /** Liga/desliga o bloqueio de tela (screen pinning) ao abrir um jogo. */
    fun definirTravaTelaJogo(ativo: Boolean) {
        travaTelaJogo = ativo
        Preferencias.definirTravaTelaJogo(getApplication<Application>(), ativo)
        atualizarTravaTela()
    }

    /**
     * Liga/desliga o silenciamento só da mídia. Reflete na hora se o Modo Jogo
     * já estiver ativo (silencia ou devolve o som).
     */
    fun definirSilenciarMidia(ativo: Boolean) {
        val app = getApplication<Application>()
        silenciarMidia = ativo
        Preferencias.definirSilenciarMidia(app, ativo)
        if (modoJogo) {
            if (ativo) SilenciarMidia.silenciar(app) else SilenciarMidia.restaurar(app)
        }
    }

    /** Liga/desliga a limpeza automática de RAM ao ativar o Modo Jogo. */
    fun definirAutoLimparRamJogo(ativo: Boolean) {
        val app = getApplication<Application>()
        autoLimparRamJogo = ativo
        Preferencias.definirAutoLimparRamJogo(app, ativo)
    }

    private fun carregarSessaoFoco() {
        val app = getApplication<Application>()
        sessaoFocoAtiva = Preferencias.sessaoFocoAtiva(app)
        if (sessaoFocoAtiva) {
            val fim = Preferencias.sessaoFocoFim(app)
            val restante = (fim - System.currentTimeMillis()) / 1000L
            if (restante <= 0) {
                encerrarSessaoFoco()
            } else {
                sessaoFocoRestanteSeg = restante
            }
        } else {
            sessaoFocoRestanteSeg = 0L
        }
    }

    /** Inicia uma sessão de foco Pomodoro (25 min) ligada ao Não Perturbe. */
    fun iniciarSessaoFoco(minutos: Int = 25) {
        val app = getApplication<Application>()
        if (!ModoConcentracao.temAcesso(app)) {
            ModoConcentracao.pedirAcesso(app)
            mensagem = "Conceda acesso ao Não Perturbe para usar a sessão de foco"
            return
        }
        val fim = System.currentTimeMillis() + minutos * 60_000L
        Preferencias.definirSessaoFocoAtiva(app, true)
        Preferencias.definirSessaoFocoFim(app, fim)
        sessaoFocoAtiva = true
        sessaoFocoRestanteSeg = minutos * 60L
        ModoConcentracao.ativar(app)
        mensagem = "Sessão de foco iniciada ($minutos min)"
        HistoricoStore.adicionar(
            app,
            HistoricoEntrada(
                tipo = HistoricoEntrada.Tipo.TURBO,
                data = System.currentTimeMillis(),
                titulo = "Sessão de foco",
                detalhe = "$minutos minutos - Não Perturbe ativado"
            )
        )
        carregarHistorico()
    }

    /** Encerra a sessão de foco e restaura o Não Perturbe. */
    fun encerrarSessaoFoco() {
        val app = getApplication<Application>()
        Preferencias.definirSessaoFocoAtiva(app, false)
        Preferencias.definirSessaoFocoFim(app, 0L)
        sessaoFocoAtiva = false
        sessaoFocoRestanteSeg = 0L
        ModoConcentracao.restaurar(app)
        mensagem = "Sessão de foco encerrada"
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
        val app = getApplication<Application>()
        modoJogo = ativo
        if (dndAuto) {
            if (ativo) {
                if (!ModoConcentracao.ativar(app)) {
                    dndAcesso = false
                    ModoConcentracao.pedirAcesso(app)
                }
            } else {
                ModoConcentracao.restaurar(app)
            }
        }
        if (silenciarMidia) {
            if (ativo) SilenciarMidia.silenciar(app)
            else SilenciarMidia.restaurar(app)
        }
        if (ativo && autoLimparRamJogo) {
            val apps = GerenciadorRam.appsFechaveis(app)
            if (apps.isNotEmpty()) {
                GerenciadorRam.liberar(app, apps.map { it.pacote })
                memoriasEncerradas(apps.size)
            }
        }
        HistoricoStore.adicionar(
            app,
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
