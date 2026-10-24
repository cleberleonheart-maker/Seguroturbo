package com.seguraturbo.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object TurboAgendador {
    private const val PEDIDO_LIGAR = 3001
    private const val PEDIDO_DESLIGAR = 3002

    fun agendar(ctx: Context) {
        cancelar(ctx)
        if (!Preferencias.turboAgendadoAtivo(ctx)) return
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return

        agendarAlarme(am, ctx, PEDIDO_LIGAR, Preferencias.turboAgendadoHoraLigar(ctx), Preferencias.turboAgendadoMinLigar(ctx), TurboAgendadoReceiver.ACAO_LIGAR)
        agendarAlarme(am, ctx, PEDIDO_DESLIGAR, Preferencias.turboAgendadoHoraDesligar(ctx), Preferencias.turboAgendadoMinDesligar(ctx), TurboAgendadoReceiver.ACAO_DESLIGAR)
    }

    fun cancelar(ctx: Context) {
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return
        am.cancel(pendente(ctx, PEDIDO_LIGAR, TurboAgendadoReceiver.ACAO_LIGAR))
        am.cancel(pendente(ctx, PEDIDO_DESLIGAR, TurboAgendadoReceiver.ACAO_DESLIGAR))
    }

    private fun agendarAlarme(am: AlarmManager, ctx: Context, pedido: Int, hora: Int, min: Int, acao: String) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora.coerceIn(0, 23))
            set(Calendar.MINUTE, min.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        try {
            val pi = pendente(ctx, pedido, acao)
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        } catch (_: Exception) {
            try {
                val pi = pendente(ctx, pedido, acao)
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            } catch (_: Exception) {
            }
        }
    }

    private fun pendente(ctx: Context, pedido: Int, acao: String): PendingIntent {
        val intent = Intent(ctx, TurboAgendadoReceiver::class.java).setAction(acao)
        return PendingIntent.getBroadcast(
            ctx,
            pedido,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
