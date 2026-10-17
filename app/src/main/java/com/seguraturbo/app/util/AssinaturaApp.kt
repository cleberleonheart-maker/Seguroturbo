package com.seguraturbo.app.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

/**
 * Lê o certificado de assinatura do próprio app e devolve o SHA-256 em hex
 * (formato "AA:BB:..."). Serve para o usuário conferir que o APK é autêntico.
 */
object AssinaturaApp {

    fun sha256(ctx: Context): String? = try {
        val pm = ctx.packageManager
        val bytesAssinatura = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pm.getPackageInfo(ctx.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                .signingInfo?.apkContentsSigners?.firstOrNull()
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(ctx.packageName, PackageManager.GET_SIGNATURES)
                .signatures?.firstOrNull()
        }
        bytesAssinatura?.let { assinatura ->
            MessageDigest.getInstance("SHA-256")
                .digest(assinatura.toByteArray())
                .joinToString(":") { "%02X".format(it) }
        }
    } catch (e: Exception) {
        null
    }
}
