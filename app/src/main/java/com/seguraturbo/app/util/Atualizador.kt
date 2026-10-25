package com.seguraturbo.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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

/** Resultado da verificação de atualização, distinguindo "sem novidade" de falha. */
sealed class ResultadoVerificacao {
    /** Há uma versão mais nova publicada. */
    data class Disponivel(val atualizacao: Atualizacao) : ResultadoVerificacao()

    /** A consulta funcionou e a versão instalada já é a mais recente. */
    object Atualizado : ResultadoVerificacao()

    /** Não foi possível consultar (rede, limite da API etc.). */
    object Erro : ResultadoVerificacao()
}

/**
 * Verifica no GitHub se há uma versão mais nova que a instalada.
 * Não baixa nem instala nada por conta própria: apenas avisa e leva o usuário
 * até a página de download, que ele confirma manualmente.
 */
object Atualizador {

    private const val API =
        "https://api.github.com/repos/cleberleonheart-maker/Seguroturbo/releases/latest"

    /** Página que redireciona para a última release (usada como fallback sem API). */
    private const val ULTIMA_RELEASE =
        "https://github.com/cleberleonheart-maker/Seguroturbo/releases/latest"

    /** Base dos downloads da release, para montar a URL do APK no fallback. */
    private const val DOWNLOAD_BASE =
        "https://github.com/cleberleonheart-maker/Seguroturbo/releases/download"

    /** Quantos redirecionamentos seguimos ao baixar o APK (o GitHub usa 302 para o CDN). */
    private const val MAX_REDIRECIONAMENTOS = 6

    /** Versão instalada do app (versionName). */
    @Suppress("DEPRECATION")
    fun versaoInstalada(ctx: Context): String = try {
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "0"
    } catch (e: Exception) {
        "0"
    }

    /**
     * Consulta o último release e devolve o resultado da verificação, distinguindo
     * "já atualizado" de falha de rede/consulta. Deve rodar fora da thread principal.
     */
    fun verificarResultado(ctx: Context): ResultadoVerificacao {
        val remoto = ultimaVersaoRemota() ?: return ResultadoVerificacao.Erro
        return if (comparar(remoto.versao, versaoInstalada(ctx)) > 0) {
            ResultadoVerificacao.Disponivel(remoto)
        } else {
            ResultadoVerificacao.Atualizado
        }
    }

    /**
     * Consulta o último release e, se for mais novo que o instalado, devolve os
     * dados da atualização. Em caso de erro de rede ou de leitura devolve null.
     * Prefira [verificarResultado] quando precisar diferenciar erro de "sem novidade".
     */
    fun verificar(ctx: Context): Atualizacao? =
        (verificarResultado(ctx) as? ResultadoVerificacao.Disponivel)?.atualizacao

    /**
     * Obtém a última versão publicada. Tenta a API do GitHub e, se ela falhar
     * (limite de requisições, bloqueio, rede), cai para o redirecionamento da
     * página `/releases/latest`, que não usa a API. Devolve null só se ambos falharem.
     */
    private fun ultimaVersaoRemota(): Atualizacao? = viaApi() ?: viaRedirecionamento()

    private fun viaApi(): Atualizacao? {
        val json = baixar() ?: return null
        val tag = json.optString("tag_name").removePrefix("v").trim()
        if (tag.isEmpty()) return null

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

    /**
     * Fallback sem API: segue o redirecionamento de `/releases/latest` para
     * descobrir a tag e monta as URLs do APK e da página da release.
     */
    private fun viaRedirecionamento(): Atualizacao? {
        val tag = tagDoRedirecionamento() ?: return null
        return Atualizacao(
            versao = tag,
            notas = "",
            apkUrl = "$DOWNLOAD_BASE/v$tag/SeguroTurbo-v$tag.apk",
            pagina = "https://github.com/cleberleonheart-maker/SeguroTurbo/releases/tag/v$tag"
        )
    }

    /** Lê o cabeçalho Location de `/releases/latest` e extrai a tag (ex.: "1.36"). */
    private fun tagDoRedirecionamento(): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(ULTIMA_RELEASE).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 15000
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", "SeguroTurbo-Android")
            }
            val codigo = conn.responseCode
            if (codigo in 300..399) {
                extrairTag(conn.getHeaderField("Location"))
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            runCatching { conn?.disconnect() }
        }
    }

    /** Extrai a tag de uma URL de redirecionamento, ex.: ".../releases/tag/v1.36" → "1.36". */
    internal fun extrairTag(location: String?): String? {
        if (location.isNullOrBlank()) return null
        val trecho = location.trimEnd('/').substringAfterLast('/')
        val tag = trecho.removePrefix("v")
        return tag.takeIf { it.isNotBlank() && it.first().isDigit() }
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
     * Abre uma conexão HTTP seguindo manualmente os redirecionamentos (3xx).
     * O GitHub responde o link da release com um 302 para um CDN assinado e a
     * implementação padrão do Android não segue esse salto entre hosts, o que
     * fazia o download falhar. Aqui seguimos até [MAX_REDIRECIONAMENTOS] saltos.
     */
    private fun conectar(url: String, cabecalhos: Map<String, String> = emptyMap()): HttpURLConnection {
        var atual = url
        var saltos = 0
        while (true) {
            val conn = (URL(atual).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", "SeguroTurbo-Android")
                cabecalhos.forEach { (chave, valor) -> setRequestProperty(chave, valor) }
            }
            val codigo = conn.responseCode
            if (codigo in 300..399) {
                val destino = conn.getHeaderField("Location")
                conn.disconnect()
                if (destino.isNullOrBlank() || saltos >= MAX_REDIRECIONAMENTOS) {
                    throw IOException("Redirecionamento inválido ou em excesso ($codigo)")
                }
                atual = URL(URL(atual), destino).toString()
                saltos++
                continue
            }
            return conn
        }
    }

    /**
     * Baixa o APK da atualização para o cache do app, informando o progresso (0–100;
     * -1 quando o tamanho total é desconhecido). Devolve o arquivo ou null em caso de erro.
     * A rede roda em [Dispatchers.IO], então pode ser chamada da thread principal.
     */
    suspend fun baixarApk(
        ctx: Context,
        url: String,
        onProgress: suspend (Int) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext null
        var conn: HttpURLConnection? = null
        try {
            conn = conectar(url)
            if (conn.responseCode !in 200..299) return@withContext null
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

    /**
     * Baixa o JSON do último release. Passa por [conectar] para seguir os
     * redirecionamentos que a API do GitHub devolve (ex.: repositório renomeado),
     * que a implementação padrão do Android não acompanha entre hosts.
     */
    private fun baixar(): JSONObject? {
        var conn: HttpURLConnection? = null
        return try {
            conn = conectar(API, mapOf("Accept" to "application/vnd.github+json"))
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
