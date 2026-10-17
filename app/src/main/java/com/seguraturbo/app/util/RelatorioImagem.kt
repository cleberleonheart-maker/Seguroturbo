package com.seguraturbo.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as CorAndroid
import android.graphics.Paint
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Gera uma imagem (PNG) com o resultado de um scan/benchmark e devolve um Uri
 * pronto para ser compartilhado. A imagem é desenhada no próprio aparelho,
 * nada é enviado para fora.
 */
object RelatorioImagem {

    private const val LARGURA = 1080
    private const val ALTURA = 1500

    fun gerar(
        ctx: Context,
        titulo: String,
        subtitulo: String,
        pontuacao: Int?,
        linhas: List<Pair<String, String>>
    ): Uri? {
        val bitmap = Bitmap.createBitmap(LARGURA, ALTURA, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Fundo
        canvas.drawColor(CorAndroid.parseColor("#05070A"))

        // Barra neon no topo
        paint.color = CorAndroid.parseColor("#00F2FF")
        canvas.drawRect(0f, 0f, LARGURA.toFloat(), 14f, paint)

        // Título
        paint.color = CorAndroid.parseColor("#00F2FF")
        paint.textSize = 62f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(titulo, 64f, 130f, paint)

        // Subtítulo
        paint.color = CorAndroid.parseColor("#B4C0DE")
        paint.textSize = 32f
        paint.isFakeBoldText = false
        canvas.drawText(subtitulo, 64f, 182f, paint)

        var y = 300f
        if (pontuacao != null) {
            paint.color = corDaPontuacao(pontuacao)
            paint.textSize = 120f
            paint.isFakeBoldText = true
            canvas.drawText("$pontuacao", 64f, y, paint)
            val larguraNumero = paint.measureText("$pontuacao")
            paint.color = CorAndroid.parseColor("#B4C0DE")
            paint.textSize = 44f
            canvas.drawText("/100", 64f + larguraNumero + 10f, y, paint)
            y += 90f
        }

        paint.textSize = 36f
        for ((rotulo, valor) in linhas) {
            if (y > ALTURA - 140f) break
            paint.isFakeBoldText = false
            paint.color = CorAndroid.parseColor("#B4C0DE")
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(rotulo, 64f, y, paint)

            paint.isFakeBoldText = true
            paint.color = CorAndroid.parseColor("#E7ECFF")
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(valor, LARGURA - 64f, y, paint)

            y += 62f
        }

        // Rodapé
        paint.textAlign = Paint.Align.LEFT
        paint.color = CorAndroid.parseColor("#4A5568")
        paint.textSize = 28f
        paint.isFakeBoldText = false
        canvas.drawText("SeguroTurbo • Segurança • Desempenho • Benchmark", 64f, ALTURA - 60f, paint)

        val dir = File(ctx.cacheDir, "relatorios").apply { mkdirs() }
        val arquivo = File(dir, "seguraturbo_${System.currentTimeMillis()}.png")
        return try {
            FileOutputStream(arquivo).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", arquivo)
        } catch (e: Exception) {
            bitmap.recycle()
            null
        }
    }

    fun compartilhar(ctx: Context, uri: Uri, texto: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, texto)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(
            Intent.createChooser(intent, texto).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun corDaPontuacao(score: Int): Int = when {
        score >= 80 -> CorAndroid.parseColor("#00FF88")
        score >= 50 -> CorAndroid.parseColor("#FFD600")
        else -> CorAndroid.parseColor("#FF0055")
    }
}
