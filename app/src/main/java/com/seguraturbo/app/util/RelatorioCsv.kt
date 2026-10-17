package com.seguraturbo.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.seguraturbo.app.model.HistoricoEntrada
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exporta o histórico de ações em CSV (separado por ponto e vírgula, aberto
 * facilmente no Excel pt-BR) e devolve o arquivo pronto para compartilhar.
 */
object RelatorioCsv {

    private val formatoData = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    /** Monta o texto CSV do histórico (mais recente primeiro). */
    internal fun montarCsv(entradas: List<HistoricoEntrada>): String {
        val sb = StringBuilder()
        sb.append("data;tipo;titulo;detalhe\n")
        entradas.sortedByDescending { it.data }.forEach { e ->
            sb.append(campo(formatoData.format(Date(e.data)))).append(';')
                .append(campo(e.tipo.name)).append(';')
                .append(campo(e.titulo)).append(';')
                .append(campo(e.detalhe)).append('\n')
        }
        return sb.toString()
    }

    fun gerar(ctx: Context, entradas: List<HistoricoEntrada>): File? {
        val dir = File(ctx.cacheDir, "relatorios").apply { mkdirs() }
        val arquivo = File(dir, "seguraturbo_historico.csv")
        return try {
            arquivo.writeText(montarCsv(entradas))
            arquivo
        } catch (e: Exception) {
            null
        }
    }

    /** Gera o CSV e abre o compartilhamento. Retorna false se não deu. */
    fun compartilhar(ctx: Context, entradas: List<HistoricoEntrada>, texto: String): Boolean {
        val arquivo = gerar(ctx, entradas) ?: return false
        val uri = try {
            FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", arquivo)
        } catch (e: Exception) {
            return false
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, texto)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return try {
            ctx.startActivity(
                Intent.createChooser(intent, texto).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Envolve o campo em aspas e escapa aspas duplas, conforme a regra do CSV. */
    private fun campo(valor: String): String = "\"" + valor.replace("\"", "\"\"") + "\""
}
