package com.seguraturbo.app.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Verifica em segundo plano (via WorkManager) se há uma versão nova no GitHub e,
 * se houver, mostra uma notificação. Roda no máximo uma vez por dia.
 */
class AtualizacaoWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val nova = withContext(Dispatchers.IO) {
            runCatching { Atualizador.verificar(applicationContext) }.getOrNull()
        } ?: return Result.success()

        if (nova.versao != Preferencias.versaoIgnorada(applicationContext)) {
            Notificacoes.mostrarAtualizacao(applicationContext, nova.versao)
        }
        return Result.success()
    }
}
