package com.seguraturbo.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

/**
 * Agenda o Não Perturbe (DND) em um período diário: liga no horário de início e
 * desliga no horário de fim. Usa [DndAgendadoReceiver] e os mesmos alarmes exatos
 * do Turbo agendado. Requer o acesso de política de notificação.
 */
object DndAgendador {
    private const val PEDIDO_LIGAR = 3101
    private const val PEDIDO_DESLIGAR = 3102

    fun agendar(ctx: Context) {
        cancelar(ctx)
        if (!Preferencias.dndAgendadoAtivo(ctx)) return
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return
        agendarAlarme(
            am, ctx, PEDIDO_LIGAR,
            Preferencias.dndHoraInicio(ctx), Preferencias.dndMinInicio(ctx),
            DndAgendadoReceiver.ACAO_LIGAR
        )
        agendarAlarme(
            am, ctx, PEDIDO_DESLIGAR,
            Preferencias.dndHoraFim(ctx), Preferencias.dndMinFim(ctx),
            DndAgendadoReceiver.ACAO_DESLIGAR
        )
    }

    fun cancelar(ctx: Context) {
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return
        am.cancel(pendente(ctx, PEDIDO_LIGAR, DndAgendadoReceiver.ACAO_LIGAR))
        am.cancel(pendente(ctx, PEDIDO_DESLIGAR, DndAgendadoReceiver.ACAO_DESLIGAR))
    }

    private fun agendarAlarme(am: AlarmManager, ctx: Context, pedido: Int, hora: Int, min: Int, acao: String) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora.coerceIn(0, 23))
            set(Calendar.MINUTE, min.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendente(ctx, pedido, acao))
        } catch (_: Exception) {
            try {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendente(ctx, pedido, acao))
            } catch (_: Exception) {
            }
        }
    }

    private fun pendente(ctx: Context, pedido: Int, acao: String): PendingIntent {
        val intent = Intent(ctx, DndAgendadoReceiver::class.java).setAction(acao)
        return PendingIntent.getBroadcast(
            ctx,
            pedido,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
