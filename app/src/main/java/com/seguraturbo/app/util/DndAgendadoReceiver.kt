package com.seguraturbo.app.util

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DndAgendadoReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val acao = intent?.action ?: return
        val ctxApp = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (acao == Intent.ACTION_BOOT_COMPLETED) {
                    if (Preferencias.dndAgendadoAtivo(ctxApp)) DndAgendador.agendar(ctxApp)
                    return@launch
                }
                if (!Preferencias.dndAgendadoAtivo(ctxApp)) return@launch
                val nm = ctxApp.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (!nm.isNotificationPolicyAccessGranted) return@launch
                when (acao) {
                    ACAO_LIGAR -> {
                        Preferencias.definirFiltroAnterior(ctxApp, nm.currentInterruptionFilter)
                        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                    }
                    ACAO_DESLIGAR -> {
                        val anterior = Preferencias.filtroAnterior(ctxApp)
                        nm.setInterruptionFilter(
                            if (anterior in NotificationManager.INTERRUPTION_FILTER_ALL
                                    ..NotificationManager.INTERRUPTION_FILTER_ALARMS
                            ) anterior else NotificationManager.INTERRUPTION_FILTER_ALL
                        )
                    }
                }
                DndAgendador.agendar(ctxApp)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACAO_LIGAR = "com.seguraturbo.app.DND_AGENDADO_LIGAR"
        const val ACAO_DESLIGAR = "com.seguraturbo.app.DND_AGENDADO_DESLIGAR"
    }
}
