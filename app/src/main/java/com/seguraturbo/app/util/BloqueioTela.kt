package com.seguraturbo.app.util

import android.app.Activity
import android.content.Context
import android.provider.Settings

/**
 * Bloqueio de tela (screen pinning) ao iniciar um jogo: fixa a tarefa atual
 * para que o jogador não saia do jogo sem querer. Para desfazer, o sistema
 * pede "toque e segure Voltar + Recentes".
 *
 * É a API pública do Android (`startLockTask`), sem permissões especiais. Numa
 * compilação de fabricante sem o recurso, a chamada vira no-op — o jogo abre
 * normalmente.
 */
object BloqueioTela {

    /** Chave (não exposta no SDK) da opção "Bloqueio de tela" das configurações. */
    private const val CHAVE_TELA_FIXADA = "lock_to_app_enabled"

    /**
     * true se o screen pinning está ligado nas configurações do sistema. Se a
     * chave não existir no aparelho, assume ligado: tentar fixar é inofensivo.
     */
    fun ativadoNoSistema(ctx: Context): Boolean = try {
        Settings.Secure.getInt(ctx.contentResolver, CHAVE_TELA_FIXADA, 1) == 1
    } catch (e: Exception) {
        true
    }

    /** Fixa a activity atual na tela. Retorna false se o sistema recusar. */
    fun fixar(activity: Activity?): Boolean {
        if (activity == null) return false
        return try {
            activity.startLockTask()
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Desfaz a fixação (usado quando o jogo não abriu e a tela ficaria presa). */
    fun desfixar(activity: Activity?) {
        if (activity == null) return
        try {
            activity.stopLockTask()
        } catch (e: Exception) {
            // não estava fixada: nada a fazer
        }
    }
}
