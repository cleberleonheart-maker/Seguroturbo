package com.seguraturbo.app.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Agenda a checagem automática de atualizações (uma vez por dia) via WorkManager.
 * Só roda quando há rede disponível.
 */
object AgendadorAtualizacao {

    private const val NOME = "seguraturbo_atualizacao"

    fun agendar(ctx: Context) {
        val pedido = PeriodicWorkRequestBuilder<AtualizacaoWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        runCatching {
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                NOME,
                ExistingPeriodicWorkPolicy.UPDATE,
                pedido
            )
        }
    }

    fun cancelar(ctx: Context) {
        runCatching { WorkManager.getInstance(ctx).cancelUniqueWork(NOME) }
    }
}
