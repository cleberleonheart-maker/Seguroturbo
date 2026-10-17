package com.seguraturbo.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Dados de uma atualização disponível no GitHub. */
data class Atualizacao(
    val versao: String,
    val notas: String,
    val apkUrl: String,
    val pagina: String
)

/**
 * Verifica no GitHub se há uma versão mais nova que a instalada.
 * Não baixa nem instala nada por conta própria: apenas avisa e leva o usuário
 * até a página de download, que ele confirma manualmente.
 */
object Atualizador {

    private const val API =
        "https://api.github.com/repos/cleberleonheart-maker/Seguroturbo/releases/latest"

    /** Versão instalada do app (versionName). */
    @Suppress("DEPRECATION")
    fun versaoInstalada(ctx: Context): String = try {
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "0"
    } catch (e: Exception) {
        "0"
    }

    /**
     * Consulta o último release e, se for mais novo que o instalado, devolve os
     * dados da atualização. Deve rodar fora da thread principal (rede).
     * Em caso de erro de rede ou de leitura devolve null.
     */
    fun verificar(ctx: Context): Atualizacao? {
        val json = baixar() ?: return null
        val tag = json.optString("tag_name").removePrefix("v").trim()
        if (tag.isEmpty()) return null
        if (comparar(tag, versaoInstalada(ctx)) <= 0) return null

        val pagina = json.optString("html_url")
        var apk = pagina
        val assets = json.optJSONArray("assets")
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val item = assets.optJSONObject(i) ?: continue
                val nome = item.optString("name")
                if (nome.endsWith(".apk") && !nome.contains("debug", ignoreCase = true)) {
                    apk = item.optString("browser_download_url").ifBlank { pagina }
                    break
                }
            }
        }

        return Atualizacao(
            versao = tag,
            notas = json.optString("body").trim(),
            apkUrl = apk,
            pagina = pagina
        )
    }

    /** Abre a URL no navegador do aparelho. */
    fun abrir(ctx: Context, url: String) {
        if (url.isBlank()) return
        try {
            ctx.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            // Nenhum app capaz de abrir o link — ignora silenciosamente.
        }
    }

    private fun baixar(): JSONObject? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(API).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "SeguroTurbo-Android")
            }
            if (conn.responseCode in 200..299) {
                JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            runCatching { conn?.disconnect() }
        }
    }

    /** Compara duas versões "x.y.z" numericamente. >0 se [a] for mais nova que [b]. */
    private fun comparar(a: String, b: String): Int {
        val pa = a.split('.', '-', '+').map { it.toIntOrNull() ?: 0 }
        val pb = b.split('.', '-', '+').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(pa.size, pb.size)) {
            val dif = pa.getOrElse(i) { 0 } - pb.getOrElse(i) { 0 }
            if (dif != 0) return dif
        }
        return 0
    }
}
