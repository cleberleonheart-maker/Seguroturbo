package com.seguraturbo.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build

/** Estado da rede atual, usado pelas checagens de segurança. */
data class EstadoRede(
    val wifiAberta: Boolean,
    val vpnAtiva: Boolean,
    val dnsPrivadoAtivo: Boolean
)

/**
 * Consulta o estado da rede do dispositivo (Wi‑Fi, VPN e DNS privado) para o
 * scan de segurança. Tudo é obtido por APIs do sistema — nada de rede própria.
 */
object RedeSeguranca {

    fun analisar(ctx: Context): EstadoRede = EstadoRede(
        wifiAberta = wifiAberta(ctx),
        vpnAtiva = vpnAtiva(ctx),
        dnsPrivadoAtivo = dnsPrivadoAtivo(ctx)
    )

    /**
     * true quando o Wi‑Fi conectado não usa senha. A identificação do tipo de
     * segurança só é possível no Android 12+; em versões antigas retorna false
     * (evitamos alarme falso).
     */
    @Suppress("DEPRECATION")
    private fun wifiAberta(ctx: Context): Boolean = try {
        val wifi = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val info: WifiInfo? = wifi?.connectionInfo
        if (info == null || info.networkId == -1) {
            false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            info.currentSecurityType == WifiInfo.SECURITY_TYPE_OPEN
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }

    private fun vpnAtiva(ctx: Context): Boolean = try {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.allNetworks.any { rede ->
            cm.getNetworkCapabilities(rede)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        }
    } catch (e: Exception) {
        false
    }

    /** true quando o DNS privado (DoT/DoH) está ativo no sistema (Android 9+). */
    private fun dnsPrivadoAtivo(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        return try {
            val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val rede = cm.activeNetwork ?: return false
            cm.getLinkProperties(rede)?.isPrivateDnsActive == true
        } catch (e: Exception) {
            false
        }
    }
}
