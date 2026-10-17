package com.seguraturbo.app.ui.benchmark

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seguraturbo.app.model.BenchmarkResult
import com.seguraturbo.app.model.HistoricoEntrada
import com.seguraturbo.app.util.BenchmarkEngine
import com.seguraturbo.app.util.BenchmarkStore
import com.seguraturbo.app.util.DeviceInfo
import com.seguraturbo.app.util.HistoricoStore
import kotlinx.coroutines.launch

/**
 * Estado da tela de Benchmark. Coleta informações reais do aparelho e roda um
 * teste de CPU de verdade, registrando o resultado no histórico.
 */
class BenchmarkViewModel(app: Application) : AndroidViewModel(app) {

    var testando by mutableStateOf(false)
    var progresso by mutableStateOf(0)
    var resultado by mutableStateOf<BenchmarkResult?>(null)
    var info by mutableStateOf<DeviceInfo.Info?>(null)
    var historico by mutableStateOf<List<HistoricoEntrada>>(emptyList())
    var pontuacoes by mutableStateOf<List<Int>>(emptyList())

    init {
        info = DeviceInfo.coletar(getApplication<Application>())
        carregarHistorico()
        pontuacoes = BenchmarkStore.listar(getApplication<Application>())
    }

    private fun carregarHistorico() {
        historico = HistoricoStore.listar(getApplication<Application>())
    }

    fun iniciar() {
        if (testando) return
        viewModelScope.launch {
            testando = true
            progresso = 0
            resultado = null
            val r = BenchmarkEngine.runBenchmark(getApplication<Application>()) { progresso = it }
            resultado = r
            testando = false
            BenchmarkStore.adicionar(getApplication<Application>(), r.total)
            pontuacoes = BenchmarkStore.listar(getApplication<Application>())
            HistoricoStore.adicionar(
                getApplication<Application>(),
                HistoricoEntrada(
                    tipo = HistoricoEntrada.Tipo.BENCHMARK,
                    data = System.currentTimeMillis(),
                    titulo = "${r.total} pontos",
                    detalhe = "CPU ${r.cpu} · GPU ${r.gpu} · MEM ${r.mem} · UX ${r.ux}"
                )
            )
            carregarHistorico()
            info = DeviceInfo.coletar(getApplication<Application>())
        }
    }

    fun limparHistorico() {
        HistoricoStore.limpar(getApplication<Application>())
        BenchmarkStore.limpar(getApplication<Application>())
        pontuacoes = emptyList()
        carregarHistorico()
    }
}
