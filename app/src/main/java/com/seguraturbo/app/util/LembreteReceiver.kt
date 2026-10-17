package com.seguraturbo.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Recebe o alarme periódico e mostra o lembrete de verificação. */
class LembreteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (Preferencias.lembreteAtivo(context)) {
            Notificacoes.mostrarLembrete(context)
        }
    }
}
