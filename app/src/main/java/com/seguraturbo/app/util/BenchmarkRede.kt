package com.seguraturbo.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Teste de rede real: mede a **latência** (ida e volta) e a **velocidade de
 * download** contra os endpoints públicos do Cloudflare Speed Test.
 *
 * Dicas honestas: os números variam com a rede, o horário e o Wi‑Fi/dados
 * móveis; o objetivo é dar uma noção, não um valor de laboratório.
 */
object BenchmarkRede {

    /** Resultado do teste (cada campo é null quando não foi possível medir). */
    data class Resultado(val latenciaMs: Long?, val downloadMbps: Double?) {
        val temDados: Boolean get() = latenciaMs != null || downloadMbps != null
    }

    private const val URL_LATENCIA = "https://speed.cloudflare.com/__down?bytes=0"
    private const val URL_DOWNLOAD = "https://speed.cloudflare.com/__down?bytes=10000000"

    /** Quantas vezes tentamos a latência (pegamos a melhor amostra). */
    private const val TENTATIVAS_LATENCIA = 3

    /** Tempo máximo baixando, em segundos, para não demorar demais. */
    private const val LIMITE_DOWNLOAD_S = 8.0

    suspend fun executar(): Resultado = withContext(Dispatchers.IO) {
        Resultado(latenciaMs = medirLatencia(), downloadMbps = medirDownload())
    }

    /** Converte bytes baixados em megabits por segundo. */
    internal fun mbps(bytes: Long, segundos: Double): Double =
        if (segundos <= 0.0) 0.0 else (bytes * 8.0 / 1_000_000.0) / segundos

    /** Melhor (menor) latência entre as amostras; null se a lista estiver vazia. */
    internal fun melhorLatencia(amostras: List<Long>): Long? = amostras.minOrNull()

    private fun medirLatencia(): Long? {
        val amostras = mutableListOf<Long>()
        repeat(TENTATIVAS_LATENCIA) {
            medirUmaLatencia()?.let { amostras += it }
        }
        return melhorLatencia(amostras)
    }

    private fun medirUmaLatencia(): Long? = try {
        val inicio = System.nanoTime()
        val conexao = (URL(URL_LATENCIA).openConnection() as HttpURLConnection).apply {
            connectTimeout = 5000
            readTimeout = 5000
            requestMethod = "GET"
        }
        conexao.inputStream.use { it.readBytes() }
        val ms = (System.nanoTime() - inicio) / 1_000_000
        conexao.disconnect()
        ms
    } catch (e: Exception) {
        null
    }

    private fun medirDownload(): Double? = try {
        val inicio = System.nanoTime()
        val conexao = (URL(URL_DOWNLOAD).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10000
            readTimeout = 15000
            requestMethod = "GET"
        }
        var total = 0L
        conexao.inputStream.use { entrada ->
            val buffer = ByteArray(64 * 1024)
            var lidos = entrada.read(buffer)
            while (lidos != -1 &&
                (System.nanoTime() - inicio) < (LIMITE_DOWNLOAD_S * 1_000_000_000).toLong()
            ) {
                total += lidos
                lidos = entrada.read(buffer)
            }
        }
        val segundos = (System.nanoTime() - inicio) / 1_000_000_000.0
        conexao.disconnect()
        if (total <= 0L) null else mbps(total, segundos)
    } catch (e: Exception) {
        null
    }
}
