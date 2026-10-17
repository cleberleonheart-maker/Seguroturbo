package com.seguraturbo.app.util

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * Controla o modo "Não Perturbe" (Do Not Disturb) do sistema, usado pelo Game
 * Turbo para silenciar notificações enquanto o Modo Jogo está ativo.
 *
 * Alterar o modo de interrupção exige o acesso de política de notificação
 * (o mesmo do "Não Perturbe" nas configurações), concedido pelo usuário.
 */
object ModoConcentracao {

    /** true se o app pode alterar o modo de interrupção do sistema. */
    fun temAcesso(ctx: Context): Boolean = try {
        (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .isNotificationPolicyAccessGranted
    } catch (e: Exception) {
        false
    }

    /** Indica se o Não Perturbe está ativo no momento. */
    fun estaAtivo(ctx: Context): Boolean = try {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.isNotificationPolicyAccessGranted &&
                nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
    } catch (e: Exception) {
        false
    }

    /**
     * Ativa o Não Perturbe e guarda o modo anterior para restaurar depois.
     * Retorna true apenas se conseguiu alterar (isto é, há acesso).
     */
    fun ativar(ctx: Context): Boolean = try {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            false
        } else {
            Preferencias.definirFiltroAnterior(ctx, nm.currentInterruptionFilter)
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            true
        }
    } catch (e: Exception) {
        false
    }

    /** Restaura o modo de interrupção anterior (ou "tudo" por padrão). */
    fun restaurar(ctx: Context) {
        try {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!nm.isNotificationPolicyAccessGranted) return
            val anterior = Preferencias.filtroAnterior(ctx)
            nm.setInterruptionFilter(
                if (anterior in NotificationManager.INTERRUPTION_FILTER_ALL
                        ..NotificationManager.INTERRUPTION_FILTER_ALARMS
                ) anterior else NotificationManager.INTERRUPTION_FILTER_ALL
            )
        } catch (e: Exception) {
            // Sem acesso ou serviço indisponível — ignora.
        }
    }

    /** Abre a tela de acesso à política de notificação (Não Perturbe). */
    fun pedirAcesso(ctx: Context) {
        try {
            ctx.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            // Sem tela correspondente — ignora.
        }
    }
}
