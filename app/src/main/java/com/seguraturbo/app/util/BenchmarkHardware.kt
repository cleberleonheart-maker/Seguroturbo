package com.seguraturbo.app.util

import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Medições de hardware de verdade (fora da thread principal).
 *
 *  - GPU: renderização offscreen com OpenGL ES 2.0 (pbuffer), contando quadros.
 *  - Armazenamento: escrita + leitura sequencial de um arquivo temporário.
 */
object BenchmarkHardware {

    /**
     * Mede quadros por segundo num contexto OpenGL ES offscreen.
     * Retorna 0 se o dispositivo/EGL não permitir (nesse caso quem chamou usa estimativa).
     */
    fun medirGpuFps(duracaoMs: Long = 600): Int {
        var display: EGLDisplay = EGL14.EGL_NO_DISPLAY
        var context: EGLContext = EGL14.EGL_NO_CONTEXT
        var surface: EGLSurface = EGL14.EGL_NO_SURFACE
        try {
            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (display == EGL14.EGL_NO_DISPLAY) return 0

            val versao = IntArray(2)
            if (!EGL14.eglInitialize(display, versao, 0, versao, 1)) return 0

            val configAttribs = intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfig = IntArray(1)
            if (!EGL14.eglChooseConfig(display, configAttribs, 0, configs, 0, 1, numConfig, 0) ||
                numConfig[0] == 0
            ) return 0
            val config = configs[0] ?: return 0

            val ctxAttribs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
            context = EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, ctxAttribs, 0)
            if (context == EGL14.EGL_NO_CONTEXT) return 0

            val surfAttribs = intArrayOf(
                EGL14.EGL_WIDTH, 256,
                EGL14.EGL_HEIGHT, 256,
                EGL14.EGL_NONE
            )
            surface = EGL14.eglCreatePbufferSurface(display, config, surfAttribs, 0)
            if (surface == EGL14.EGL_NO_SURFACE) return 0
            if (!EGL14.eglMakeCurrent(display, surface, surface, context)) return 0

            GLES20.glViewport(0, 0, 256, 256)

            val fim = System.nanoTime() + duracaoMs * 1_000_000
            var quadros = 0
            while (System.nanoTime() < fim) {
                GLES20.glClearColor(quadros % 2 * 0.5f, 0.2f, 0.8f, 1f)
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                GLES20.glFinish()
                quadros++
            }
            return (quadros * 1000L / duracaoMs).toInt()
        } catch (e: Throwable) {
            return 0
        } finally {
            try {
                if (display != EGL14.EGL_NO_DISPLAY) {
                    EGL14.eglMakeCurrent(
                        display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT
                    )
                    if (surface != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(display, surface)
                    if (context != EGL14.EGL_NO_CONTEXT) EGL14.eglDestroyContext(display, context)
                    EGL14.eglTerminate(display)
                }
            } catch (e: Throwable) {
                // ignora erros de limpeza
            }
        }
    }

    /**
     * Mede a velocidade sequencial do armazenamento interno (escrita + leitura).
     * Retorna MB/s combinados, ou 0.0 em caso de falha.
     */
    fun medirArmazenamentoMbs(ctx: Context, megabytes: Int = 4): Double {
        val arquivo = File(ctx.cacheDir, "bench_io.tmp")
        val bloco = ByteArray(128 * 1024)
        val totalBytes = megabytes * 1024 * 1024
        return try {
            val inicio = System.nanoTime()

            FileOutputStream(arquivo).use { out ->
                var escrito = 0
                while (escrito < totalBytes) {
                    out.write(bloco)
                    escrito += bloco.size
                }
                out.flush()
                out.fd.sync()
            }

            FileInputStream(arquivo).use { ins ->
                val buffer = ByteArray(128 * 1024)
                while (ins.read(buffer) != -1) {
                    // apenas lê tudo
                }
            }

            val segundos = (System.nanoTime() - inicio) / 1_000_000_000.0
            if (segundos <= 0.0) 0.0
            else (totalBytes * 2.0 / (1024.0 * 1024.0)) / segundos
        } catch (e: Exception) {
            0.0
        } finally {
            runCatching { arquivo.delete() }
        }
    }
}
