package com.seguraturbo.app.util

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.UserManager

/**
 * Detecção **somente leitura** do perfil de dispositivo: se o aparelho é
 * gerenciado (Device Owner / Profile Owner), se está num perfil de trabalho
 * (work profile) e se há apps com política empresarial. Nada é alterado e
 * nenhum dado é coletado — apenas consultamos APIs públicas do Android.
 */
object PerfilDispositivo {

    /** Fatos coletados do aparelho, usados na classificação (função pura). */
    data class Fatos(
        val deviceOwner: Boolean,
        val profileOwner: Boolean,
        val perfilTrabalho: Boolean,
        val gerenciado: Boolean
    )

    /** Resultado legível para a UI: rótulo e explicação já resolvidos. */
    data class Diagnostico(
        val tipo: Tipo,
        val deviceOwner: Boolean,
        val profileOwner: Boolean,
        val perfilTrabalho: Boolean
    )

    /** Classificação do tipo de gerenciamento do aparelho. */
    enum class Tipo {
        PESSOAL,
        PERFIL_TRABALHO,
        GERENCIADO
    }

    /**
     * A partir dos fatos, decide o tipo de perfil. Um aparelho com Device Owner
     * ou Profile Owner é "gerenciado"; um com apenas um perfil de trabalho é
     * "perfil de trabalho"; caso contrário, é pessoal.
     */
    fun classificar(f: Fatos): Diagnostico {
        val tipo = when {
            f.deviceOwner || f.profileOwner || f.gerenciado -> Tipo.GERENCIADO
            f.perfilTrabalho -> Tipo.PERFIL_TRABALHO
            else -> Tipo.PESSOAL
        }
        return Diagnostico(
            tipo = tipo,
            deviceOwner = f.deviceOwner,
            profileOwner = f.profileOwner,
            perfilTrabalho = f.perfilTrabalho
        )
    }

    /** Coleta os fatos do aparelho (somente leitura) e devolve o diagnóstico. */
    fun coletar(ctx: Context): Diagnostico {
        val dpm = try {
            ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        } catch (e: Exception) {
            null
        }

        val deviceOwner = dpm?.isDeviceOwnerApp(ctx.packageName) == true
        // Um app de terceiros não consegue consultar o owner de outro pacote, mas
        // o app instalado neste contexto pode ter políticas próprias ativas.
        val profileOwner = dpm?.isProfileOwnerApp(ctx.packageName) == true

        // `isUserManaged` só existe a partir do Android 11 e não é exposto de forma
        // estável em todas as versões; usamos apenas as APIs confiáveis abaixo.
        val gerenciado = false

        val perfilTrabalho = try {
            val um = ctx.getSystemService(Context.USER_SERVICE) as? UserManager
            um?.isManagedProfile == true
        } catch (e: Exception) {
            false
        }

        return classificar(
            Fatos(
                deviceOwner = deviceOwner,
                profileOwner = profileOwner,
                perfilTrabalho = perfilTrabalho,
                gerenciado = gerenciado
            )
        )
    }

    /**
     * Há indícios de gerenciamento corporativo? Usado para decidir se vale a
     * pena mostrar o cartão/observação na tela de Segurança.
     */
    fun ehGerenciado(d: Diagnostico): Boolean =
        d.tipo != Tipo.PESSOAL || d.deviceOwner || d.profileOwner || d.perfilTrabalho

    /** Nome do pacote do app de administração, se visível (informativo). */
    fun temAdminAtivo(ctx: Context): Boolean = try {
        val dpm = ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        dpm?.activeAdmins?.isNotEmpty() == true
    } catch (e: Exception) {
        false
    }
}
