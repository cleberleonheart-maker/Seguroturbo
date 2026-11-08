package com.seguraturbo.app.util

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Alarme exato no Android 12+ exige a permissão SCHEDULE_EXACT_ALARM concedida
 * pelo usuário. Sem ela, os agendamentos caem para alarme inexato (pode atrasar).
 */
object AlarmeExato {

    /** true se é possível agendar alarme exato neste aparelho agora. */
    fun podeAgendar(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return false
        return am.canScheduleExactAlarms()
    }

    /** Abre a tela do sistema para o usuário permitir alarmes exatos. */
    fun pedirPermissao(ctx: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        if (podeAgendar(ctx)) return
        try {
            ctx.startActivity(
                Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:${ctx.packageName}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
            // Aparelho sem essa tela — segue com o alarme inexato.
        }
    }
}
