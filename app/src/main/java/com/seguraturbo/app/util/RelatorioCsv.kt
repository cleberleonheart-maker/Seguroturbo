package com.seguraturbo.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.seguraturbo.app.model.HistoricoEntrada
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Exporta o histórico de ações em CSV (separado por ponto e vírgula, aberto
 * facilmente no Excel pt-BR) e devolve o arquivo pronto para compartilhar.
 */
object RelatorioCsv {

    /** Monta o texto CSV do histórico (mais recente primeiro). */
    internal fun montarCsv(entradas: List<HistoricoEntrada>): String {
        val sb = StringBuilder()
        sb.append("data;tipo;titulo;detalhe\n")
        entradas.sortedByDescending { it.data }.forEach { e ->
            sb.append(CsvUtil.campo(CsvUtil.dataHora(e.data))).append(';')
                .append(CsvUtil.campo(e.tipo.name)).append(';')
                .append(CsvUtil.campo(e.titulo)).append(';')
                .append(CsvUtil.campo(e.detalhe)).append('\n')
        }
        return sb.toString()
    }

    /** Gera o arquivo CSV no cache (roda fora da main thread). */
    suspend fun gerar(ctx: Context, entradas: List<HistoricoEntrada>): File? =
        withContext(Dispatchers.IO) {
            val dir = File(ctx.cacheDir, "relatorios").apply { mkdirs() }
            val arquivo = File(dir, "seguraturbo_historico.csv")
            try {
                arquivo.writeText(montarCsv(entradas))
                arquivo
            } catch (e: Exception) {
                null
            }
        }

    /** Gera o CSV e abre o compartilhamento. Retorna false se não deu. */
    suspend fun compartilhar(ctx: Context, entradas: List<HistoricoEntrada>, texto: String): Boolean {
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
}
