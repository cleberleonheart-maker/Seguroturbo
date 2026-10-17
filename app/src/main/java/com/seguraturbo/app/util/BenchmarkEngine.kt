package com.seguraturbo.app.util

import android.content.Context
import com.seguraturbo.app.model.BenchmarkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Benchmark com medições REAIS do aparelho:
 *  - CPU: laço de cálculo cronometrado
 *  - GPU: renderização OpenGL ES offscreen (FPS)
 *  - Armazenamento: escrita/leitura sequencial (MB/s) — usado no índice de UX
 *  - MEM: estimativa a partir da RAM total real
 */
object BenchmarkEngine {

    /**
     * Valor "sorvedouro": garante que o compilador/JIT não elimine o laço de
     * medição da CPU por o resultado nunca ser lido. Volatile impede a remoção.
     */
    @Volatile
    private var blackhole: Double = 0.0

    suspend fun runBenchmark(ctx: Context, onProgress: (Int) -> Unit): BenchmarkResult {
        val info = DeviceInfo.coletar(ctx)

        onProgress(5)

        // CPU: medição real (fora da thread principal para não travar a interface).
        val operacoes = withContext(Dispatchers.Default) { medirCpu(duracaoMs = 350) }
        val cpu = normalizar(operacoes, 20_000L, 400_000L, 80_000, 250_000)
        onProgress(25)

        // GPU: renderização real OpenGL ES; se falhar, estimativa por núcleos.
        val fps = withContext(Dispatchers.Default) { BenchmarkHardware.medirGpuFps(duracaoMs = 600) }
        val gpu = if (fps > 0) {
            normalizar(fps.toLong(), 60L, 1200L, 120_000, 300_000)
        } else {
            (120_000 + info.nucleos * 18_000).coerceAtMost(300_000)
        }
        onProgress(50)

        // Armazenamento: escrita + leitura reais de um arquivo temporário.
        val mbs = withContext(Dispatchers.IO) { BenchmarkHardware.medirArmazenamentoMbs(ctx, 4) }
        onProgress(75)

        // MEM: proporcional à RAM real
        val mem = (info.ramTotalMb * 120).coerceIn(60_000, 220_000).toInt()

        // UX: baseada na velocidade real do armazenamento (ou no espaço livre, se não medido).
        val ux = if (mbs > 0.0) {
            normalizar(mbs.toLong(), 40L, 900L, 100_000, 160_000)
        } else {
            (60_000 + info.armazenamentoLivreGb * 4_000).coerceIn(50_000.0, 160_000.0).toInt()
        }
        delay(250)
        onProgress(100)

        return BenchmarkResult(
            cpu = cpu,
            gpu = gpu,
            mem = mem,
            ux = ux,
            gpuFps = fps,
            armazenamentoMbs = mbs
        )
    }

    /** Executa um laço de cálculo e conta quantas rodadas couberam no tempo dado. */
    private fun medirCpu(duracaoMs: Long): Long {
        val fim = System.nanoTime() + duracaoMs * 1_000_000
        var rodadas = 0L
        var acumulador = 0.0
        while (System.nanoTime() < fim) {
            var i = 0
            while (i < 1000) {
                acumulador += i * 1.000001
                i++
            }
            rodadas++
        }
        // Grava o resultado num campo volatile: impede o JIT de eliminar o laço.
        blackhole = acumulador
        return rodadas
    }

    /** Normaliza um valor medido para uma faixa de pontuação (visível para testes). */
    internal fun normalizar(valor: Long, minimo: Long, maximo: Long, saidaMin: Int, saidaMax: Int): Int {
        val v = valor.coerceIn(minimo, maximo)
        val fracao = (v - minimo).toDouble() / (maximo - minimo).toDouble()
        return (saidaMin + fracao * (saidaMax - saidaMin)).toInt()
    }
}
