package com.seguraturbo.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TurboAgendadoReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val acao = intent?.action ?: return
        val ctxApp = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (acao == Intent.ACTION_BOOT_COMPLETED) {
                    if (Preferencias.turboAgendadoAtivo(ctxApp)) TurboAgendador.agendar(ctxApp)
                    return@launch
                }
                if (Preferencias.turboAgendadoAtivo(ctxApp)) {
                    when (acao) {
                        ACAO_LIGAR -> TurboAgendadoAplicador.ligar(ctxApp)
                        ACAO_DESLIGAR -> TurboAgendadoAplicador.desligar(ctxApp)
                    }
                }
                TurboAgendador.agendar(ctxApp)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACAO_LIGAR = "com.seguraturbo.app.TURBO_AGENDADO_LIGAR"
        const val ACAO_DESLIGAR = "com.seguraturbo.app.TURBO_AGENDADO_DESLIGAR"
    }
}
