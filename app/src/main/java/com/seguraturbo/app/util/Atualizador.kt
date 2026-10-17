package com.seguraturbo.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/** Dados de uma atualização disponível no GitHub. */
data class Atualizacao(
    val versao: String,
    val notas: String,
    /** URL direta do APK (vazia se a release não tiver APK). */
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
        var apk = ""
        val assets = json.optJSONArray("assets")
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val item = assets.optJSONObject(i) ?: continue
                val nome = item.optString("name")
                if (nome.endsWith(".apk") && !nome.contains("debug", ignoreCase = true)) {
                    apk = item.optString("browser_download_url")
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

    /** true se o app pode instalar um APK (autorização de fontes desconhecidas). */
    fun podeInstalar(ctx: Context): Boolean = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    } catch (e: Exception) {
        false
    }

    /** Abre a tela onde o usuário autoriza a instalação de apps pelo SeguroTurbo. */
    fun pedirPermissaoInstalacao(ctx: Context) {
        try {
            ctx.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${ctx.packageName}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            // Sem tela correspondente — ignora.
        }
    }

    /**
     * Baixa o APK da atualização para o cache do app, informando o progresso (0–100;
     * -1 quando o tamanho total é desconhecido). Devolve o arquivo ou null em caso de erro.
     * Deve ser chamado fora da thread principal.
     */
    suspend fun baixarApk(
        ctx: Context,
        url: String,
        onProgress: suspend (Int) -> Unit
    ): File? {
        if (url.isBlank()) return null
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "SeguroTurbo-Android")
            }
            if (conn.responseCode !in 200..299) return null
            val total = conn.contentLengthLong.takeIf { it > 0 }
            val dir = File(ctx.cacheDir, "atualizacoes").apply { mkdirs() }
            val destino = File(dir, "SeguroTurbo-atualizacao.apk")
            conn.inputStream.use { entrada ->
                FileOutputStream(destino).use { saida ->
                    val buffer = ByteArray(64 * 1024)
                    var lidos = 0L
                    var ultimo = Int.MIN_VALUE
                    while (true) {
                        val n = entrada.read(buffer)
                        if (n < 0) break
                        saida.write(buffer, 0, n)
                        lidos += n
                        val pct = if (total != null) {
                            ((lidos * 100) / total).toInt().coerceIn(0, 100)
                        } else {
                            -1
                        }
                        if (pct != ultimo) {
                            ultimo = pct
                            onProgress(pct)
                        }
                    }
                }
            }
            destino
        } catch (e: Exception) {
            null
        } finally {
            runCatching { conn?.disconnect() }
        }
    }

    /**
     * Confirma que o APK baixado é assinado com o mesmo certificado do app atual,
     * evitando instalar um arquivo adulterado.
     */
    @Suppress("DEPRECATION")
    fun assinaturaConfere(ctx: Context, apk: File): Boolean = try {
        val pm = ctx.packageManager
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            PackageManager.GET_SIGNATURES
        }
        val novo = pm.getPackageArchiveInfo(apk.absolutePath, flags)
        val atual = pm.getPackageInfo(ctx.packageName, flags)
        if (novo == null) {
            false
        } else {
            val a = assinaturas(novo)
            a.isNotEmpty() && a == assinaturas(atual)
        }
    } catch (e: Exception) {
        false
    }

    /** Abre o instalador do sistema para o APK baixado. Retorna true se abriu. */
    fun instalarApk(ctx: Context, apk: File): Boolean = try {
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", apk)
        ctx.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        true
    } catch (e: Exception) {
        false
    }

    @Suppress("DEPRECATION")
    private fun assinaturas(info: PackageInfo): Set<String> {
        val lista = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.signingInfo?.apkContentsSigners
        } else {
            info.signatures
        } ?: return emptySet()
        return lista.map { it.toCharsString() }.toSet()
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
    internal fun comparar(a: String, b: String): Int {
        val pa = a.split('.', '-', '+').map { it.toIntOrNull() ?: 0 }
        val pb = b.split('.', '-', '+').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(pa.size, pb.size)) {
            val dif = pa.getOrElse(i) { 0 } - pb.getOrElse(i) { 0 }
            if (dif != 0) return dif
        }
        return 0
    }
}
