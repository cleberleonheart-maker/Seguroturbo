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
 *  - patch de segurança do sistema (data das últimas correções)
 *  - Google Play Protect / verificação de apps
 *  - Rede: Wi‑Fi aberto, VPN e DNS privado
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
        verificarBloqueioAutomatico(ctx)?.let { encontradas += it }

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
        verificarPatchSeguranca()?.let { encontradas += it }

        delay(350)
        verificarPlayProtect(ctx)?.let { encontradas += it }

        delay(350)
        verificarRede(ctx)?.let { encontradas += it }

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
        val foraDaLoja = withContext(Dispatchers.IO) { appsForaDaLoja(ctx) }
        if (foraDaLoja.isNotEmpty()) {
            encontradas += Threat(
                nome = "${foraDaLoja.size} app(s) instalados fora da loja",
                tipo = "APPS",
                risco = "BAIXO",
                descricao = "Não vieram da Play Store: " +
                    foraDaLoja.take(3).joinToString(", ") +
                    (if (foraDaLoja.size > 3) "…" else "") +
                    ". Instale apps apenas de fontes em que você confia.",
                peso = 6
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

    /**
     * Lista apps visíveis (não de sistema) que não foram instalados pela Play
     * Store — normalmente vieram de sideload/instalação manual. Ignora o próprio
     * SeguroTurbo, que é distribuído pelo GitHub.
     */
    private fun appsForaDaLoja(ctx: Context): List<String> {
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
            .filter { it.packageName != ctx.packageName }
            .filter { instaladorDe(pm, it.packageName) != "com.android.vending" }
            .map { runCatching { it.loadLabel(pm).toString() }.getOrDefault(it.packageName) }
            .sorted()
            .toList()
    }

    /** Pacote que instalou o app (Play Store, sideload, etc.) ou null se desconhecido. */
    private fun instaladorDe(pm: PackageManager, pacote: String): String? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            pm.getInstallSourceInfo(pacote).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            pm.getInstallerPackageName(pacote)
        }
    } catch (e: Exception) {
        null
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

    /**
     * Avalia o tempo até a tela bloquear sozinha (SCREEN_OFF_TIMEOUT). Quanto
     * maior, mais tempo o aparelho fica desprotegido se for deixado de lado.
     */
    private fun verificarBloqueioAutomatico(ctx: Context): Threat? = try {
        val ms = Settings.System.getInt(
            ctx.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 30000
        )
        if (ms > 300000) {
            Threat(
                nome = "Bloqueio automático demorado",
                tipo = "TELA",
                risco = "BAIXO",
                descricao = "A tela só bloqueia após ${ms / 60000} min sem uso. " +
                    "Reduza esse tempo para proteger o aparelho.",
                acao = AcaoConfig.DISPLAY,
                peso = 6
            )
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }

    /**
     * Verifica a data do último patch de segurança do sistema (Android). Um patch
     * com mais de 6 meses indica que o aparelho pode estar exposto a falhas já
     * corrigidas em versões mais novas.
     */
    private fun verificarPatchSeguranca(): Threat? {
        val patch = Build.VERSION.SECURITY_PATCH?.trim().orEmpty()
        if (patch.isEmpty()) {
            return Threat(
                nome = "Nível de patch de segurança desconhecido",
                tipo = "SISTEMA",
                risco = "MÉDIO",
                descricao = "O sistema não informa a data das últimas correções de segurança.",
                acao = AcaoConfig.ATUALIZACAO_SISTEMA,
                peso = 12
            )
        }
        val meses = mesesDesde(patch) ?: return null
        if (meses <= 6) return null
        return Threat(
            nome = "Sistema desatualizado",
            tipo = "SISTEMA",
            risco = "MÉDIO",
            descricao = "Últimas correções de segurança de $patch (~$meses meses atrás). " +
                "Procure atualizações do sistema.",
            acao = AcaoConfig.ATUALIZACAO_SISTEMA,
            peso = 12
        )
    }

    /** Número de meses (aprox.) desde uma data no formato "yyyy-MM-dd". */
    internal fun mesesDesde(dataIso: String): Int? {
        return try {
            val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            fmt.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val quando = fmt.parse(dataIso)?.time
            if (quando == null) {
                null
            } else {
                ((System.currentTimeMillis() - quando) / (1000L * 60 * 60 * 24 * 30))
                    .toInt().coerceAtLeast(0)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Confere se o Google Play Protect está disponível. Não existe API pública
     * para ler o resultado do Protect, então verificamos a presença da Play Store
     * e a verificação de pacotes (package verifier) do sistema.
     */
    private fun verificarPlayProtect(ctx: Context): Threat? {
        if (!temApp(ctx, "com.android.vending")) {
            return Threat(
                nome = "Google Play Protect indisponível",
                tipo = "PRIVACIDADE",
                risco = "MÉDIO",
                descricao = "A Play Store não está instalada, então os apps não passam " +
                    "pela verificação automática do Google Play Protect.",
                peso = 10
            )
        }
        val verificador = try {
            Settings.Global.getInt(ctx.contentResolver, "package_verifier_enable", 1)
        } catch (e: Exception) {
            1
        }
        if (verificador == 0) {
            return Threat(
                nome = "Verificação de apps desativada",
                tipo = "PRIVACIDADE",
                risco = "MÉDIO",
                descricao = "A verificação de apps (usada pelo Google Play Protect) está " +
                    "desligada no sistema.",
                acao = AcaoConfig.SEGURANCA,
                peso = 10
            )
        }
        return null
    }

    private fun temApp(ctx: Context, pacote: String): Boolean = try {
        ctx.packageManager.getApplicationInfo(pacote, 0)
        true
    } catch (e: Exception) {
        false
    }

    /**
     * Avalia a rede atual: alerta quando o Wi‑Fi conectado não tem senha e
     * informa (na descrição) se há VPN ativa e DNS privado — recursos que
     * protegem o tráfego em redes públicas.
     */
    private fun verificarRede(ctx: Context): Threat? {
        val rede = try {
            RedeSeguranca.analisar(ctx)
        } catch (e: Exception) {
            return null
        }
        if (!rede.wifiAberta) return null

        val comVpn = rede.vpnAtiva
        val dns = if (rede.dnsPrivadoAtivo) "DNS privado ativo" else "DNS privado desativado"
        val vpn = if (comVpn) "VPN ativa" else "VPN inativa"
        return Threat(
            nome = if (comVpn) "Rede Wi‑Fi aberta (com VPN)" else "Rede Wi‑Fi aberta",
            tipo = "REDE",
            risco = if (comVpn) "BAIXO" else "MÉDIO",
            descricao = "Conectado a uma rede sem senha; o tráfego pode ser interceptado. " +
                "$vpn · $dns. Evite acessar contas e bancos enquanto estiver nessa rede.",
            acao = AcaoConfig.REDES,
            peso = if (comVpn) 5 else 12
        )
    }
}
