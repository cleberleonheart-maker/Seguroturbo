package com.seguraturbo.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Color as CorAndroid
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Gera um relatório em PDF (A4) com o resultado de um scan/benchmark e devolve
 * um Uri pronto para compartilhar. Tudo é montado no aparelho, sem enviar nada.
 */
object RelatorioPdf {

    private const val LARGURA = 595   // A4 em pontos (72 dpi)
    private const val ALTURA = 842
    private const val MARGEM = 40f

    fun gerar(
        ctx: Context,
        titulo: String,
        subtitulo: String,
        pontuacao: Int?,
        linhas: List<Pair<String, String>>
    ): Uri? {
        val documento = PdfDocument()
        val pagina = documento.startPage(
            PdfDocument.PageInfo.Builder(LARGURA, ALTURA, 1).create()
        )
        val canvas = pagina.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Fundo branco (bom para impressão) + faixa de destaque no topo.
        canvas.drawColor(CorAndroid.WHITE)
        paint.color = CorAndroid.parseColor("#00A6C8")
        canvas.drawRect(0f, 0f, LARGURA.toFloat(), 8f, paint)

        // Título
        paint.color = CorAndroid.parseColor("#10202E")
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(titulo, MARGEM, 74f, paint)

        // Subtítulo
        paint.color = CorAndroid.parseColor("#5A6B7A")
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText(subtitulo, MARGEM, 96f, paint)

        var y = 150f
        if (pontuacao != null) {
            paint.color = corDaPontuacao(pontuacao)
            paint.textSize = 42f
            paint.isFakeBoldText = true
            canvas.drawText("$pontuacao/100", MARGEM, y, paint)
            y += 46f
        }

        paint.textSize = 13f
        for ((rotulo, valor) in linhas) {
            if (y > ALTURA - 70f) break
            paint.isFakeBoldText = false
            paint.color = CorAndroid.parseColor("#33404F")
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(rotulo, MARGEM, y, paint)

            paint.isFakeBoldText = true
            paint.color = CorAndroid.parseColor("#10202E")
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(valor, LARGURA - MARGEM, y, paint)

            y += 24f
        }

        // Rodapé
        paint.textAlign = Paint.Align.LEFT
        paint.isFakeBoldText = false
        paint.color = CorAndroid.parseColor("#9AA4B2")
        paint.textSize = 10f
        canvas.drawText("SeguroTurbo • Segurança • Desempenho • Benchmark", MARGEM, ALTURA - 30f, paint)

        documento.finishPage(pagina)

        val dir = File(ctx.cacheDir, "relatorios").apply { mkdirs() }
        val arquivo = File(dir, "seguraturbo_${System.currentTimeMillis()}.pdf")
        return try {
            FileOutputStream(arquivo).use { documento.writeTo(it) }
            documento.close()
            FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", arquivo)
        } catch (e: Exception) {
            documento.close()
            null
        }
    }

    fun compartilhar(ctx: Context, uri: Uri, texto: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, texto)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(
            Intent.createChooser(intent, texto).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun corDaPontuacao(score: Int): Int = when {
        score >= 80 -> CorAndroid.parseColor("#0A8F4D")
        score >= 50 -> CorAndroid.parseColor("#B8860B")
        else -> CorAndroid.parseColor("#C2183C")
    }
}
