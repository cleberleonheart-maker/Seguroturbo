package com.seguraturbo.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/**
 * Agenda/cancela o lembrete de verificação usando um alarme inexato semanal
 * (não exige permissão de alarme exato e é amigável à bateria).
 */
object Agendador {

    private const val PEDIDO = 2001
    private const val SETE_DIAS = AlarmManager.INTERVAL_DAY * 7

    fun agendar(ctx: Context) {
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return
        val quando = System.currentTimeMillis() + SETE_DIAS
        try {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, quando, SETE_DIAS, pendente(ctx))
        } catch (e: Exception) {
            // Ignora falhas de agendamento.
        }
    }

    fun cancelar(ctx: Context) {
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return
        am.cancel(pendente(ctx))
    }

    private fun pendente(ctx: Context): PendingIntent {
        val intent = Intent(ctx, LembreteReceiver::class.java)
        return PendingIntent.getBroadcast(
            ctx,
            PEDIDO,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
