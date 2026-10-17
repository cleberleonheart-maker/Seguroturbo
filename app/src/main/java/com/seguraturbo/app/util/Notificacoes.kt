package com.seguraturbo.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.seguraturbo.app.MainActivity
import com.seguraturbo.app.R

/** Canal e envio do lembrete de verificação de segurança. */
object Notificacoes {

    const val CANAL = "seguraturbo_lembrete"
    private const val ID_LEMBRETE = 1001

    fun criarCanal(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = ctx.getSystemService(NotificationManager::class.java) ?: return
            if (mgr.getNotificationChannel(CANAL) == null) {
                val canal = NotificationChannel(
                    CANAL,
                    "Lembretes",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Lembretes de verificação de segurança"
                }
                mgr.createNotificationChannel(canal)
            }
        }
    }

    fun mostrarLembrete(ctx: Context) {
        criarCanal(ctx)
        // Sem permissão (Android 13+), não há o que mostrar.
        if (!NotificationManagerCompat.from(ctx).areNotificationsEnabled()) return

        val abrir = Intent(ctx, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            ctx,
            0,
            abrir,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacao = NotificationCompat.Builder(ctx, CANAL)
            .setSmallIcon(R.drawable.ic_security)
            .setContentTitle(ctx.getString(R.string.notif_titulo))
            .setContentText(ctx.getString(R.string.notif_texto))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(ctx).notify(ID_LEMBRETE, notificacao)
        } catch (e: SecurityException) {
            // Permissão revogada entre a checagem e o envio.
        }
    }
}
