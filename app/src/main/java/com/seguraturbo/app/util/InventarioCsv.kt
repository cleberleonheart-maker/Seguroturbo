package com.seguraturbo.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exporta o inventário de apps instalados e as permissões sensíveis que cada um
 * tem concedidas, em CSV (separado por ponto e vírgula, fácil de abrir no Excel
 * pt-BR). Reaproveita a auditoria de permissões já existente — tudo local, sem
 * enviar dados para lugar nenhum.
 */
object InventarioCsv {

    private val formatoNome = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.getDefault())

    /**
     * Monta o texto CSV do inventário. Função pura (sem Android): recebe os apps e
     * uma função que traduz o rótulo de cada grupo de permissão.
     */
    internal fun montarCsv(
        apps: List<AppComPermissoes>,
        rotulo: (GrupoPermissao) -> String
    ): String {
        val sb = StringBuilder()
        sb.append("pacote;nome;permissoes_sensiveis\n")
        apps.sortedBy { it.nome.lowercase() }.forEach { app ->
            sb.append(campo(app.pacote)).append(';')
                .append(campo(app.nome)).append(';')
                .append(campo(app.grupos.joinToString(", ") { rotulo(it) }))
                .append('\n')
        }
        return sb.toString()
    }

    /** Gera o arquivo CSV do inventário no cache. Retorna null se não deu. */
    fun gerar(
        ctx: Context,
        apps: List<AppComPermissoes>,
        rotulo: (GrupoPermissao) -> String
    ): File? {
        val dir = File(ctx.cacheDir, "relatorios").apply { mkdirs() }
        val arquivo = File(dir, "seguraturbo_inventario.csv")
        return try {
            arquivo.writeText(montarCsv(apps, rotulo))
            arquivo
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gera o CSV do inventário e abre o compartilhamento. [rotulo] traduz os grupos
     * (normalmente `{ ctx.getString(it.rotuloRes) }`). Retorna false se falhar.
     */
    fun compartilhar(
        ctx: Context,
        apps: List<AppComPermissoes>,
        rotulo: (GrupoPermissao) -> String,
        texto: String
    ): Boolean {
        val arquivo = gerar(ctx, apps, rotulo) ?: return false
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

    /** Nome sugerido do arquivo, com a data (ex.: seguraturbo_inventario_2026-10-20_1430.csv). */
    fun nomeSugerido(): String = "seguraturbo_inventario_${formatoNome.format(Date())}.csv"

    private fun campo(valor: String): String = "\"" + valor.replace("\"", "\"\"") + "\""
}
