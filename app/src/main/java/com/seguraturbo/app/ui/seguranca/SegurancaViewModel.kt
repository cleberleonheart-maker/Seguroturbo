package com.seguraturbo.app.ui.seguranca

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seguraturbo.app.model.HistoricoEntrada
import com.seguraturbo.app.model.Threat
import com.seguraturbo.app.util.AppComPermissoes
import com.seguraturbo.app.util.AuditoriaPermissoes
import com.seguraturbo.app.util.Formato
import com.seguraturbo.app.util.HistoricoStore
import com.seguraturbo.app.util.Preferencias
import com.seguraturbo.app.util.RiscoApp
import com.seguraturbo.app.util.SecurityScanner
import com.seguraturbo.app.util.StorageCleaner
import com.seguraturbo.app.widget.SeguroTurboWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Guarda o estado da tela de Segurança. A lógica saiu dos composables e agora
 * vive aqui, sobrevivendo a rotações e recomposições.
 */
class SegurancaViewModel(app: Application) : AndroidViewModel(app) {

    var escaneando by mutableStateOf(false)
    var ameacas by mutableStateOf<List<Threat>>(emptyList())
    var jaEscaneou by mutableStateOf(false)
    var score by mutableStateOf<Int?>(null)
    var limpando by mutableStateOf(false)
    var progressoLimpeza by mutableStateOf(0)
    var mensagemLimpeza by mutableStateOf<String?>(null)
    var historico by mutableStateOf<List<HistoricoEntrada>>(emptyList())
    var appsEmRisco by mutableStateOf<List<AppComPermissoes>>(emptyList())

    init {
        carregarHistorico()
        carregarPerfisRisco()
    }

    private fun carregarHistorico() {
        historico = HistoricoStore.listar(getApplication<Application>())
    }

    fun carregarPerfisRisco() {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) { AuditoriaPermissoes.listar(getApplication<Application>()) }
            appsEmRisco = apps.filter { AuditoriaPermissoes.riscoDe(it) != RiscoApp.NENHUM }
        }
    }

    fun escanear() {
        if (escaneando || limpando) return
        viewModelScope.launch {
            escaneando = true
            jaEscaneou = true
            ameacas = emptyList()
            val resultado = SecurityScanner.scan(getApplication<Application>()) { encontrada ->
                ameacas = ameacas + encontrada
            }
            ameacas = resultado.ameacas
            score = resultado.score
            escaneando = false
            // Guarda o último score (para o widget) e atualiza o widget.
            Preferencias.definirUltimoScore(getApplication<Application>(), resultado.score)
            SeguroTurboWidget.atualizarTodos(getApplication<Application>())
            HistoricoStore.adicionar(
                getApplication<Application>(),
                HistoricoEntrada(
                    tipo = HistoricoEntrada.Tipo.SCAN,
                    data = System.currentTimeMillis(),
                    titulo = if (resultado.ameacas.isEmpty()) "Dispositivo seguro"
                    else "${resultado.ameacas.size} alerta(s)",
                    detalhe = "Pontuação ${resultado.score}/100 · " +
                        resultado.ameacas.joinToString(", ") { it.nome }
                            .ifBlank { "Nenhum problema encontrado" }
                )
            )
            carregarHistorico()
            carregarPerfisRisco()
        }
    }

    fun limpar() {
        if (limpando || escaneando) return
        viewModelScope.launch {
            limpando = true
            mensagemLimpeza = null
            progressoLimpeza = 0
            val liberado = withContext(Dispatchers.IO) {
                StorageCleaner.limparCache(getApplication<Application>())
            }
            for (i in 1..100) {
                progressoLimpeza = i
                delay(8)
            }
            mensagemLimpeza = "${Formato.tamanho(liberado)} liberados"
            limpando = false
            HistoricoStore.adicionar(
                getApplication<Application>(),
                HistoricoEntrada(
                    tipo = HistoricoEntrada.Tipo.LIMPEZA,
                    data = System.currentTimeMillis(),
                    titulo = "Limpeza de cache",
                    detalhe = mensagemLimpeza ?: ""
                )
            )
            carregarHistorico()
            // Reavalia o cache após a limpeza.
            jaEscaneou = false
        }
    }

    fun limparHistorico() {
        HistoricoStore.limpar(getApplication<Application>())
        carregarHistorico()
    }
}
