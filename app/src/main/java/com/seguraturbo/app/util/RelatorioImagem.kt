package com.seguraturbo.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as CorAndroid
import android.graphics.Paint
import android.graphics.Path
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

    /**
     * Gera uma imagem (PNG) com o gráfico de evolução das pontuações do benchmark
     * e devolve um Uri pronto para compartilhar. Desenhado no próprio aparelho.
     */
    fun gerarGrafico(
        ctx: Context,
        titulo: String,
        subtitulo: String,
        pontuacoes: List<Int>
    ): Uri? {
        if (pontuacoes.isEmpty()) return null
        val bitmap = Bitmap.createBitmap(LARGURA, ALTURA, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawColor(CorAndroid.parseColor("#05070A"))
        paint.color = CorAndroid.parseColor("#00F2FF")
        canvas.drawRect(0f, 0f, LARGURA.toFloat(), 14f, paint)

        // Título e subtítulo
        paint.textAlign = Paint.Align.LEFT
        paint.isFakeBoldText = true
        paint.textSize = 62f
        canvas.drawText(titulo, 64f, 130f, paint)
        paint.color = CorAndroid.parseColor("#B4C0DE")
        paint.isFakeBoldText = false
        paint.textSize = 32f
        canvas.drawText(subtitulo, 64f, 182f, paint)

        // Última pontuação em destaque
        val ultima = pontuacoes.last()
        paint.color = corDaPontuacao(ultima)
        paint.textSize = 110f
        paint.isFakeBoldText = true
        canvas.drawText("$ultima", 64f, 320f, paint)
        val larguraNumero = paint.measureText("$ultima")
        paint.color = CorAndroid.parseColor("#B4C0DE")
        paint.textSize = 40f
        canvas.drawText("/100", 64f + larguraNumero + 10f, 320f, paint)

        // Área do gráfico
        val esq = 150f
        val dir = LARGURA - 80f
        val topo = 430f
        val base = ALTURA - 220f

        val minReal = pontuacoes.min()
        val maxReal = pontuacoes.max()
        val spanReal = (maxReal - minReal).coerceAtLeast(1)
        val margem = (spanReal * 0.15f).coerceAtLeast(1f)
        val minV = minReal - margem
        val maxV = maxReal + margem
        val span = (maxV - minV).coerceAtLeast(1f)

        fun xEm(i: Int): Float =
            if (pontuacoes.size == 1) (esq + dir) / 2f
            else esq + (dir - esq) * i.toFloat() / (pontuacoes.size - 1)

        fun yEm(v: Float): Float = base - (base - topo) * (v - minV) / span

        // Grade horizontal com rótulos
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 26f
        paint.isFakeBoldText = false
        val faixas = 4
        for (k in 0..faixas) {
            val valor = minV + span * k / faixas
            val y = yEm(valor)
            paint.color = CorAndroid.parseColor("#1B2433")
            canvas.drawLine(esq, y, dir, y, paint)
            paint.color = CorAndroid.parseColor("#4A5568")
            canvas.drawText(valor.toInt().toString(), 44f, y + 9f, paint)
        }
        paint.color = CorAndroid.parseColor("#1B2433")
        canvas.drawLine(esq, topo, esq, base, paint)

        // Linha da evolução (com brilho)
        val caminho = Path()
        pontuacoes.forEachIndexed { i, v ->
            val px = xEm(i)
            val py = yEm(v.toFloat())
            if (i == 0) caminho.moveTo(px, py) else caminho.lineTo(px, py)
        }
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = CorAndroid.parseColor("#3300F2FF")
        paint.strokeWidth = 20f
        canvas.drawPath(caminho, paint)
        paint.color = CorAndroid.parseColor("#00F2FF")
        paint.strokeWidth = 6f
        canvas.drawPath(caminho, paint)
        paint.style = Paint.Style.FILL

        // Pontos
        pontuacoes.forEachIndexed { i, v ->
            val px = xEm(i)
            val py = yEm(v.toFloat())
            paint.color = CorAndroid.parseColor("#00F2FF")
            canvas.drawCircle(px, py, 12f, paint)
            paint.color = CorAndroid.parseColor("#05070A")
            canvas.drawCircle(px, py, 5f, paint)
        }

        // Mínimo e máximo
        paint.textSize = 26f
        paint.color = CorAndroid.parseColor("#B4C0DE")
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("mín $minReal", esq, base + 46f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("máx $maxReal", dir, base + 46f, paint)

        // Rodapé
        paint.textAlign = Paint.Align.LEFT
        paint.color = CorAndroid.parseColor("#4A5568")
        paint.textSize = 28f
        canvas.drawText("SeguroTurbo • Segurança • Desempenho • Benchmark", 64f, ALTURA - 60f, paint)

        val dirCache = File(ctx.cacheDir, "relatorios").apply { mkdirs() }
        val arquivo = File(dirCache, "seguraturbo_grafico_${System.currentTimeMillis()}.png")
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
