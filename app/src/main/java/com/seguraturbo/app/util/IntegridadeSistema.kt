package com.seguraturbo.app.util

import android.content.Context
import android.os.Build
import java.io.File

/**
 * Amplia a checagem de integridade do sistema, cobrindo três eixos:
 *  - **root/superusuário**: binários `su`/Magisk e apps de root conhecidos;
 *  - **SELinux**: se está em modo *Enforcing* (recomendado) ou *Permissive*;
 *  - **bootloader/build**: indícios de build modificado (`test-keys`, `dev-keys`,
 *    `userdebug` ou `eng`), que sugerem bootloader desbloqueado.
 *
 * A coleta usa APIs públicas do Android; a classificação é isolada em [classificar]
 * para poder ser testada sem um dispositivo real.
 */
object IntegridadeSistema {

    /** Binários e caminhos típicos de root (su, Superuser, Magisk). */
    private val CAMINHOS_SU = listOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/su/bin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/adb/magisk",
        "/sbin/.magisk"
    )

    /** Gerenciadores de root/kernel conhecidos, se estiverem instalados. */
    private val PACOTES_ROOT = listOf(
        "com.topjohnwu.magisk",
        "com.noshufou.android.su",
        "com.noshufou.android.su.elite",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "com.yellowes.su",
        "com.kingroot.kinguser",
        "com.zhiqupk.root.global"
    )

    /** Estado do SELinux. */
    enum class Selinux { ENFORCING, PERMISSIVE, DESCONHECIDO }

    /** Fatos coletados do aparelho, usados na classificação. */
    data class Fatos(
        val suEncontrado: Boolean,
        val pacotesRootPresentes: List<String>,
        val tagsTestKeys: Boolean,
        val tipoBuild: String,
        val selinuxEnforcing: Boolean?
    )

    /** Resultado da análise de integridade. */
    data class Diagnostico(
        val rooteado: Boolean,
        val selinux: Selinux,
        val bootloaderSuspeito: Boolean
    )

    /**
     * Classifica os fatos coletados. Função pura (sem Android APIs), para testes.
     */
    fun classificar(f: Fatos): Diagnostico {
        val rooteado = f.suEncontrado || f.pacotesRootPresentes.isNotEmpty() || f.tagsTestKeys
        val selinux = when (f.selinuxEnforcing) {
            true -> Selinux.ENFORCING
            false -> Selinux.PERMISSIVE
            null -> Selinux.DESCONHECIDO
        }
        val buildDeTeste = f.tagsTestKeys ||
            f.tipoBuild.equals("userdebug", ignoreCase = true) ||
            f.tipoBuild.equals("eng", ignoreCase = true)
        return Diagnostico(rooteado = rooteado, selinux = selinux, bootloaderSuspeito = buildDeTeste)
    }

    /** Coleta os fatos do aparelho e devolve o diagnóstico. */
    fun coletar(ctx: Context): Diagnostico {
        val fatos = Fatos(
            suEncontrado = CAMINHOS_SU.any { File(it).exists() },
            pacotesRootPresentes = PACOTES_ROOT.filter { temPacote(ctx, it) },
            tagsTestKeys = Build.TAGS?.let {
                it.contains("test-keys") || it.contains("dev-keys")
            } == true,
            tipoBuild = Build.TYPE.orEmpty(),
            selinuxEnforcing = selinuxForcando()
        )
        return classificar(fatos)
    }

    /**
     * Estado do SELinux. Tenta ler `/sys/fs/selinux/enforce` (1=Enforcing, 0=Permissive)
     * e, se não for legível, cai para reflexão em `android.os.SELinux`. Devolve null
     * quando nada pode ser lido (não alarmamos nesse caso).
     */
    private fun selinuxForcando(): Boolean? {
        lerEnforceDoArquivo()?.let { return it }
        return selinuxPorReflexao()
    }

    private fun lerEnforceDoArquivo(): Boolean? = try {
        when (File("/sys/fs/selinux/enforce").readText().trim()) {
            "1" -> true
            "0" -> false
            else -> null
        }
    } catch (e: Exception) {
        null
    }

    private fun selinuxPorReflexao(): Boolean? = try {
        val classe = Class.forName("android.os.SELinux")
        val metodo = classe.getMethod("isSELinuxEnforced")
        metodo.invoke(null) as? Boolean
    } catch (e: Throwable) {
        null
    }

    private fun temPacote(ctx: Context, pacote: String): Boolean = try {
        ctx.packageManager.getApplicationInfo(pacote, 0)
        true
    } catch (e: Exception) {
        false
    }
}
