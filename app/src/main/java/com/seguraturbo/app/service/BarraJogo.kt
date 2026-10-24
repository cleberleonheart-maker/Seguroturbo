package com.seguraturbo.app.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.net.TrafficStats
import android.view.Choreographer
import java.io.File
import java.io.FileOutputStream

/** Ação de captura solicitada ao usuário (consentimento de projeção de tela). */
enum class AcaoCaptura { NENHUMA, CAPTURA, INICIAR_GRAVACAO, PARAR_GRAVACAO }

/**
 * Estado compartilhado da barra flutuante: guarda a projeção de tela obtida com
 * consentimento do usuário e executa captura de tela e gravação de vídeo.
 */
object BarraJogo {

    /** Ação a executar assim que o consentimento de captura for concedido. */
    var pendente: AcaoCaptura = AcaoCaptura.NENHUMA

    var gravando: Boolean = false
        private set

    /** Notifica a barra (se ativa) que o estado de gravação mudou. */
    var aoAtualizar: (() -> Unit)? = null

    private var projecao: MediaProjection? = null
    private var gravador: MediaRecorder? = null
    private var displayGravacao: VirtualDisplay? = null
    private var arquivoGravacao: java.io.File? = null
    private var inicioGravacao = 0L

    fun temProjecao(): Boolean = projecao != null

    fun definirProjecao(p: MediaProjection?) {
        projecao = p
    }

    private fun avisar() {
        aoAtualizar?.invoke()
    }

    fun liberar() {
        try { displayGravacao?.release() } catch (e: Exception) { }
        displayGravacao = null
        try { gravador?.stop(); gravador?.release() } catch (e: Exception) { }
        gravador = null
        gravando = false
        try { projecao?.stop() } catch (e: Exception) { }
        projecao = null
    }

    private fun pastaSaida(ctx: Context): java.io.File {
        val base = ctx.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            ?: ctx.filesDir
        val dir = java.io.File(base, "GameTurbo")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** Captura um quadro da tela e salva como PNG. */
    fun capturar(ctx: Context, onResultado: (java.io.File?) -> Unit) {
        val mp = projecao
        if (mp == null) { onResultado(null); return }
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = android.util.DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        val largura = metrics.widthPixels
        val altura = metrics.heightPixels
        val densidade = metrics.densityDpi
        val leitor = android.media.ImageReader.newInstance(
            largura, altura, android.graphics.PixelFormat.RGBA_8888, 2
        )
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        var vd: VirtualDisplay? = null
        leitor.setOnImageAvailableListener({ r ->
            val imagem = try { r.acquireLatestImage() } catch (e: Exception) { null }
            if (imagem != null) {
                try {
                    val plano = imagem.planes[0]
                    val buffer = plano.buffer
                    val pixelStride = plano.pixelStride
                    val rowStride = plano.rowStride
                    val rowPadding = rowStride - pixelStride * largura
                    val bitmap = android.graphics.Bitmap.createBitmap(
                        largura + rowPadding / pixelStride, altura,
                        android.graphics.Bitmap.Config.ARGB_8888
                    )
                    buffer.rewind()
                    bitmap.copyPixelsFromBuffer(buffer)
                    val recorte = android.graphics.Bitmap.createBitmap(bitmap, 0, 0, largura, altura)
                    val f = java.io.File(pastaSaida(ctx), "captura_${System.currentTimeMillis()}.png")
                    java.io.FileOutputStream(f).use {
                        recorte.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                    }
                    bitmap.recycle()
                    recorte.recycle()
                    onResultado(f)
                } catch (e: Exception) {
                    onResultado(null)
                } finally {
                    imagem.close()
                    try { vd?.release() } catch (e: Exception) { }
                    try { leitor.close() } catch (e: Exception) { }
                }
            }
        }, handler)
        try {
            vd = mp.createVirtualDisplay(
                "seguraturbo_captura", largura, altura, densidade,
                android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                leitor.surface, null, handler
            )
        } catch (e: Exception) {
            onResultado(null)
        }
    }

    /** Inicia a gravação de vídeo (MP4) da tela. */
    fun iniciarGravacao(ctx: Context): Boolean {
        val mp = projecao ?: return false
        if (gravando) return true
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = android.util.DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        val largura = metrics.widthPixels
        val altura = metrics.heightPixels
        val densidade = metrics.densityDpi
        val arquivo = java.io.File(pastaSaida(ctx), "gravacao_${System.currentTimeMillis()}.mp4")
        return try {
            val rec = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                android.media.MediaRecorder(ctx)
            } else {
                @Suppress("DEPRECATION")
                android.media.MediaRecorder()
            }
            rec.setVideoSource(android.media.MediaRecorder.VideoSource.SURFACE)
            rec.setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4)
            rec.setVideoEncoder(android.media.MediaRecorder.VideoEncoder.H264)
            rec.setVideoSize(largura, altura)
            rec.setVideoFrameRate(30)
            rec.setVideoEncodingBitRate(6_000_000)
            rec.setOutputFile(arquivo.absolutePath)
            rec.prepare()
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            displayGravacao = mp.createVirtualDisplay(
                "seguraturbo_gravacao", largura, altura, densidade,
                android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                rec.surface, null, handler
            )
            rec.start()
            gravador = rec
            arquivoGravacao = arquivo
            inicioGravacao = System.currentTimeMillis()
            gravando = true
            avisar()
            true
        } catch (e: Exception) {
            try { gravador?.release() } catch (ex: Exception) { }
            gravador = null
            false
        }
    }

    /** Encerra a gravação e devolve o arquivo gerado. */
    fun pararGravacao(): java.io.File? {
        if (!gravando) return null
        val arquivo = arquivoGravacao
        try { gravador?.stop() } catch (e: Exception) { }
        try { gravador?.release() } catch (e: Exception) { }
        gravador = null
        try { displayGravacao?.release() } catch (e: Exception) { }
        displayGravacao = null
        gravando = false
        arquivoGravacao = null
        avisar()
        return arquivo
    }

    /** Segundos decorridos da gravação atual. */
    fun duracaoGravacao(): Int =
        if (gravando) ((System.currentTimeMillis() - inicioGravacao) / 1000L).toInt() else 0
}

    // Monitor FPS e Rede em tempo real
    var mostrarFpsRede by mutableStateOf(false)
    private var ultimoFrameTime = 0L
    private var frames = 0
    private var fpsAtual = 0f
    private val frameCallback = object : android.view.Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            frames++
            val agora = frameTimeNanos / 1_000_000L
            if (ultimoFrameTime == 0L) ultimoFrameTime = agora
            if (agora - ultimoFrameTime >= 1000) {
                fpsAtual = frames * 1000f / (agora - ultimoFrameTime)
                frames = 0
                ultimoFrameTime = agora
            }
            android.view.Choreographer.getInstance().postFrameCallback(this)
        }
    }

    fun iniciarMonitorFps() {
        try {
            android.view.Choreographer.getInstance().postFrameCallback(frameCallback)
        } catch (_: Exception) {}
    }

    fun pararMonitorFps() {
        try {
            android.view.Choreographer.getInstance().removeFrameCallback(frameCallback)
        } catch (_: Exception) {}
        fpsAtual = 0f
        frames = 0
        ultimoFrameTime = 0L
    }

    fun fps(): Float = fpsAtual

    private var rxAnt = 0L
    private var txAnt = 0L
    private var tempoRede = 0L
    private var rxKbps = 0f
    private var txKbps = 0f

    fun atualizarRede() {
        try {
            val rx = android.net.TrafficStats.getTotalRxBytes()
            val tx = android.net.TrafficStats.getTotalTxBytes()
            val agora = System.currentTimeMillis()
            if (tempoRede > 0 && agora - tempoRede > 0) {
                val dt = (agora - tempoRede) / 1000f
                rxKbps = (rx - rxAnt).toFloat() / 1024f / dt
                txKbps = (tx - txAnt).toFloat() / 1024f / dt
                if (rxKbps < 0) rxKbps = 0f
                if (txKbps < 0) txKbps = 0f
            }
            rxAnt = rx
            txAnt = tx
            tempoRede = agora
        } catch (_: Exception) {}
    }

    fun redeRxKbps(): Float = rxKbps
    fun redeTxKbps(): Float = txKbps
