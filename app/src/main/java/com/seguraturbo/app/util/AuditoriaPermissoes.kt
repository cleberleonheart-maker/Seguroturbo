package com.seguraturbo.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.seguraturbo.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Grupo de permissões sensíveis, com o rótulo traduzível e as permissões do
 * sistema que fazem parte dele. Agrupar dá uma leitura melhor do que listar
 * cada permissão crua (ex.: "Câmera" em vez de "android.permission.CAMERA").
 */
enum class GrupoPermissao(val rotuloRes: Int, val permissoes: List<String>) {
    CAMERA(R.string.perm_camera, listOf(
        "android.permission.CAMERA"
    )),
    MICROFONE(R.string.perm_microfone, listOf(
        "android.permission.RECORD_AUDIO"
    )),
    LOCALIZACAO(R.string.perm_localizacao, listOf(
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION"
    )),
    SMS(R.string.perm_sms, listOf(
        "android.permission.READ_SMS",
        "android.permission.SEND_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.RECEIVE_MMS"
    )),
    CONTATOS(R.string.perm_contatos, listOf(
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS"
    )),
    CHAMADAS(R.string.perm_chamadas, listOf(
        "android.permission.READ_CALL_LOG",
        "android.permission.WRITE_CALL_LOG"
    )),
    TELEFONE(R.string.perm_telefone, listOf(
        "android.permission.READ_PHONE_STATE",
        "android.permission.READ_PHONE_NUMBERS"
    )),
    SENSORES(R.string.perm_sensores, listOf(
        "android.permission.BODY_SENSORS"
    )),
    CALENDARIO(R.string.perm_calendario, listOf(
        "android.permission.READ_CALENDAR",
        "android.permission.WRITE_CALENDAR"
    ))
}

/** Um app com os grupos de permissões sensíveis que ele realmente tem concedidos. */
data class AppComPermissoes(
    val pacote: String,
    val nome: String,
    val grupos: List<GrupoPermissao>,
    val temInternet: Boolean = false
)

/**
 * Nível de risco de um app, cruzando o acesso à internet com as permissões
 * sensíveis que ele tem. Um app que pode enviar dados para fora **e** acessa
 * dados sensíveis merece mais atenção do que um que só faz uma das duas coisas.
 */
enum class RiscoApp(val peso: Int) {
    NENHUM(0),
    BAIXO(1),
    MEDIO(2),
    ALTO(3)
}

/**
 * Auditoria de permissões: descobre quais apps visíveis têm acesso concedido a
 * câmera, microfone, localização, SMS, contatos etc. No Android 11+ só
 * enxergamos os apps visíveis (regra do sistema), o que já cobre os instalados
 * pelo usuário.
 */
object AuditoriaPermissoes {

    /** Grupos de dados mais sensíveis, cujo vazamento causa mais dano. */
    private val GRUPOS_CRITICOS = setOf(
        GrupoPermissao.MICROFONE,
        GrupoPermissao.LOCALIZACAO,
        GrupoPermissao.SMS,
        GrupoPermissao.CHAMADAS,
        GrupoPermissao.CONTATOS
    )

    /** Lista os apps (não de sistema) com pelo menos um grupo sensível concedido. */
    suspend fun listar(ctx: Context): List<AppComPermissoes> = withContext(Dispatchers.IO) {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolvidos = try {
            pm.queryIntentActivities(intent, 0)
        } catch (e: Exception) {
            return@withContext emptyList()
        }
        resolvidos.asSequence()
            .mapNotNull { it.activityInfo?.applicationInfo }
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .distinctBy { it.packageName }
            .filter { it.packageName != ctx.packageName }
            .mapNotNull { app ->
                val grupos = gruposConcedidos(pm, app.packageName)
                if (grupos.isEmpty()) {
                    null
                } else {
                    AppComPermissoes(
                        app.packageName,
                        rotulo(pm, app),
                        grupos,
                        temInternet = temInternet(pm, app.packageName)
                    )
                }
            }
            .sortedBy { it.nome.lowercase() }
            .toList()
    }

    /** Se o app pede a permissão de internet (pode enviar dados para fora). */
    fun temInternet(pm: PackageManager, pacote: String): Boolean = try {
        @Suppress("DEPRECATION")
        val info = pm.getPackageInfo(pacote, PackageManager.GET_PERMISSIONS)
        info.requestedPermissions?.contains("android.permission.INTERNET") == true
    } catch (e: Exception) {
        false
    }

    /**
     * Calcula o risco de um app a partir dos grupos concedidos e do acesso à
     * internet. Função pura (usada na UI e nos testes).
     *
     * Regra: internet + vários dados críticos = ALTO; internet + 1 dado crítico
     * (ou vários dados sem internet) = MÉDIO; só internet ou só um dado = BAIXO.
     */
    fun risco(grupos: List<GrupoPermissao>, temInternet: Boolean): RiscoApp {
        val criticos = grupos.count { it in GRUPOS_CRITICOS }
        return when {
            temInternet && criticos >= 2 -> RiscoApp.ALTO
            temInternet && criticos == 1 -> RiscoApp.MEDIO
            !temInternet && criticos >= 3 -> RiscoApp.MEDIO
            temInternet || grupos.isNotEmpty() -> RiscoApp.BAIXO
            else -> RiscoApp.NENHUM
        }
    }

    /** Risco de um app já montado (atalho para a UI). */
    fun riscoDe(app: AppComPermissoes): RiscoApp = risco(app.grupos, app.temInternet)

    /** Quantos apps há em cada nível de risco (mais alto primeiro). */
    fun resumoRisco(apps: List<AppComPermissoes>): List<Pair<RiscoApp, Int>> =
        RiscoApp.values().reversed()
            .map { risco -> risco to apps.count { riscoDe(it) == risco } }
            .filter { it.second > 0 }

    /** Grupos cujas permissões o app realmente tem concedidas. */
    fun gruposConcedidos(pm: PackageManager, pacote: String): List<GrupoPermissao> =
        GrupoPermissao.values().filter { grupo ->
            grupo.permissoes.any { permissao ->
                try {
                    pm.checkPermission(permissao, pacote) == PackageManager.PERMISSION_GRANTED
                } catch (e: Exception) {
                    false
                }
            }
        }

    /** Grupos presentes numa lista de permissões (pura, usada nos testes). */
    fun gruposDe(permissoes: Set<String>): List<GrupoPermissao> =
        GrupoPermissao.values().filter { grupo -> grupo.permissoes.any { it in permissoes } }

    /** Quantos apps têm cada grupo — usado no resumo do diálogo. */
    fun resumo(apps: List<AppComPermissoes>): List<Pair<GrupoPermissao, Int>> =
        GrupoPermissao.values()
            .map { grupo -> grupo to apps.count { grupo in it.grupos } }
            .filter { it.second > 0 }

    private fun rotulo(pm: PackageManager, app: ApplicationInfo): String =
        runCatching { app.loadLabel(pm).toString() }.getOrDefault(app.packageName)
}
