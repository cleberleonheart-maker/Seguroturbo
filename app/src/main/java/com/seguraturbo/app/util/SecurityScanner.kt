package com.seguraturbo.app.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.seguraturbo.app.model.AcaoConfig
import com.seguraturbo.app.model.Threat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Scanner de segurança com verificações REAIS do dispositivo:
 *  - root / superusuário
 *  - bloqueio de tela
 *  - serviços de acessibilidade ativos
 *  - instalação de fontes desconhecidas
 *  - opções de desenvolvedor
 *  - apps com permissões sensíveis
 *  - cache/temporários acumulados
 *
 * (Não é um antivírus: não analisa assinaturas de malware — é educativo.)
 */
object SecurityScanner {

    data class Resultado(val ameacas: List<Threat>, val score: Int)

    /** Permissões consideradas sensíveis para a listagem informativa. */
    private val PERMISSOES_SENSIVEIS = listOf(
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.READ_SMS",
        "android.permission.SEND_SMS",
        "android.permission.READ_CONTACTS",
        "android.permission.READ_CALL_LOG",
        "android.permission.READ_PHONE_STATE",
        "android.permission.BODY_SENSORS",
        "android.permission.READ_CALENDAR"
    )

    suspend fun scan(ctx: Context, onThreatFound: (Threat) -> Unit): Resultado {
        val encontradas = mutableListOf<Threat>()

        delay(350)
        if (estaRooteado()) {
            encontradas += Threat(
                nome = "Acesso root detectado",
                tipo = "SISTEMA",
                risco = "ALTO",
                descricao = "O dispositivo aparenta estar rooteado, o que reduz a proteção do sistema.",
                peso = 30
            )
        }

        delay(350)
        if (!temBloqueioDeTela(ctx)) {
            encontradas += Threat(
                nome = "Sem bloqueio de tela",
                tipo = "SISTEMA",
                risco = "ALTO",
                descricao = "Nenhum PIN, senha ou biometria protege o aparelho.",
                acao = AcaoConfig.SEGURANCA,
                peso = 25
            )
        }

        delay(350)
        val servicosAcessibilidade = withContext(Dispatchers.IO) { servicosAcessibilidadeAtivos(ctx) }
        if (servicosAcessibilidade > 0) {
            encontradas += Threat(
                nome = "Serviços de acessibilidade ativos",
                tipo = "ACESSIBILIDADE",
                risco = "MÉDIO",
                descricao = "$servicosAcessibilidade serviço(s) podem ler a tela. Habilite " +
                    "apenas o que você reconhece.",
                acao = AcaoConfig.ACESSIBILIDADE,
                peso = 15
            )
        }

        delay(350)
        if (fontesDesconhecidas(ctx)) {
            encontradas += Threat(
                nome = "Fontes desconhecidas ativadas",
                tipo = "CONFIGURAÇÃO",
                risco = "MÉDIO",
                descricao = "A instalação de apps de fora da loja oficial está permitida.",
                acao = AcaoConfig.FONTES_DESCONHECIDAS,
                peso = 15
            )
        }

        delay(350)
        if (opcoesDesenvolvedor(ctx)) {
            encontradas += Threat(
                nome = "Opções de desenvolvedor ativas",
                tipo = "CONFIGURAÇÃO",
                risco = "BAIXO",
                descricao = "Depuração/opções de desenvolvedor estão habilitadas.",
                acao = AcaoConfig.OPCOES_DESENVOLVEDOR,
                peso = 8
            )
        }

        delay(350)
        val appsSensiveis = withContext(Dispatchers.IO) { appsComPermissoesSensiveis(ctx) }
        if (appsSensiveis.isNotEmpty()) {
            encontradas += Threat(
                nome = "${appsSensiveis.size} app(s) com permissões sensíveis",
                tipo = "PRIVACIDADE",
                risco = "BAIXO",
                descricao = "Acesso a câmera, microfone, localização ou mensagens: " +
                    appsSensiveis.take(3).joinToString(", ") +
                    if (appsSensiveis.size > 3) "…" else "",
                peso = 5
            )
        }

        delay(350)
        val cache = withContext(Dispatchers.IO) { StorageCleaner.calcularCacheBytes(ctx) }
        if (cache > 0) {
            encontradas += Threat(
                nome = "Arquivos temporários acumulados",
                tipo = "LIXO",
                risco = "BAIXO",
                descricao = "${Formato.tamanho(cache)} de cache podem ser liberados.",
                acao = AcaoConfig.CACHE,
                peso = 5
            )
        }

        encontradas.forEach { onThreatFound(it) }
        val score = (100 - encontradas.sumOf { it.peso }).coerceIn(0, 100)
        return Resultado(encontradas, score)
    }

    /** Verifica se há indícios de root no aparelho. */
    fun estaRooteado(): Boolean {
        val caminhos = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su"
        )
        if (caminhos.any { File(it).exists() }) return true
        return Build.TAGS?.contains("test-keys") == true
    }

    private fun temBloqueioDeTela(ctx: Context): Boolean = try {
        val km = ctx.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        km.isDeviceSecure
    } catch (e: Exception) {
        true // na dúvida, não alarmar
    }

    private fun servicosAcessibilidadeAtivos(ctx: Context): Int = try {
        val am = ctx.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK).size
    } catch (e: Exception) {
        0
    }

    /**
     * Lista os apps com atividade de launcher que pedem permissões sensíveis.
     * Enxergamos apenas os apps visíveis (regra do Android 11+), o que é suficiente.
     */
    private fun appsComPermissoesSensiveis(ctx: Context): List<String> {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolvidos = try {
            pm.queryIntentActivities(intent, 0)
        } catch (e: Exception) {
            return emptyList()
        }
        return resolvidos.asSequence()
            .mapNotNull { it.activityInfo?.applicationInfo }
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .distinctBy { it.packageName }
            .filter { app -> appPedePermissaoSensivel(pm, app.packageName) }
            .map { runCatching { it.loadLabel(pm).toString() }.getOrDefault(it.packageName) }
            .sorted()
            .toList()
    }

    @Suppress("DEPRECATION")
    private fun appPedePermissaoSensivel(pm: PackageManager, pacote: String): Boolean = try {
        val info = pm.getPackageInfo(pacote, PackageManager.GET_PERMISSIONS)
        val pedidas = info.requestedPermissions?.toSet() ?: emptySet()
        pedidas.any { it in PERMISSOES_SENSIVEIS }
    } catch (e: Exception) {
        false
    }

    /**
     * A partir do Android 8 (API 26) a instalação de fontes desconhecidas é por app,
     * consultada via PackageManager. A antiga Settings.Secure.INSTALL_NON_MARKET_APPS
     * está depreciada e sempre retorna 0 no Android moderno.
     */
    private fun fontesDesconhecidas(ctx: Context): Boolean = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.packageManager.canRequestPackageInstalls()
        } else {
            @Suppress("DEPRECATION")
            Settings.Secure.getInt(ctx.contentResolver, Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1
        }
    } catch (e: Exception) {
        false
    }

    private fun opcoesDesenvolvedor(ctx: Context): Boolean = try {
        Settings.Global.getInt(ctx.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
    } catch (e: Exception) {
        false
    }
}
