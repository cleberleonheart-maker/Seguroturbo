package com.seguraturbo.app.util

import android.content.Context
import android.media.AudioManager

/**
 * Silencia apenas a mídia (músicas, vídeos e o som dos jogos) durante o Modo
 * Jogo, sem mexer em chamadas, alarmes e notificações. Usa o stream público
 * [AudioManager.STREAM_MUSIC]; nenhuma permissão é necessária.
 *
 * O volume anterior é guardado nas preferências para ser restaurado depois.
 */
object SilenciarMidia {

    /** true se o stream de mídia está silenciado. */
    fun estaMudo(ctx: Context): Boolean = try {
        audioManager(ctx)?.isStreamMute(AudioManager.STREAM_MUSIC) ?: false
    } catch (e: Exception) {
        false
    }

    /**
     * Baixa o volume da mídia a zero, guardando o volume anterior (só na
     * primeira vez, para não sobrescrever com 0). Retorna true se conseguiu.
     */
    fun silenciar(ctx: Context): Boolean {
        val am = audioManager(ctx) ?: return false
        return try {
            if (!am.isStreamMute(AudioManager.STREAM_MUSIC)) {
                Preferencias.definirVolumeMidia(ctx, am.getStreamVolume(AudioManager.STREAM_MUSIC))
            }
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Restaura o volume de mídia salvo. Se não houver valor salvo válido, usa
     * metade do volume máximo para não devolver o som no talo. Retorna true se
     * alterou algo (ou seja, se estava silenciado).
     */
    fun restaurar(ctx: Context): Boolean {
        val am = audioManager(ctx) ?: return false
        if (!am.isStreamMute(AudioManager.STREAM_MUSIC)) return false
        val alvo = volumeAoRestaurar(Preferencias.volumeMidia(ctx), am.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
        return try {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, alvo, 0)
            Preferencias.definirVolumeMidia(ctx, -1)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Volume de destino ao restaurar: o valor salvo, se for válido, senão metade
     * do máximo (nunca devolve o som no talo nem deixa mudo). Função pura.
     */
    fun volumeAoRestaurar(salvo: Int, maximo: Int): Int =
        if (salvo in 1..maximo) salvo else (maximo / 2).coerceAtLeast(1)

    private fun audioManager(ctx: Context): AudioManager? = try {
        ctx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    } catch (e: Exception) {
        null
    }
}
