package com.seguraturbo.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.seguraturbo.app.MainActivity
import com.seguraturbo.app.R

/**
 * Publica atalhos dinâmicos (toque longo no ícone) para cada jogo com perfil
 * personalizado. Ao tocar, o atalho abre o jogo já com a barra do Game Turbo.
 */
object AtalhosJogos {

    private const val PREFIXO = "jogo_"

    /** Recria os atalhos dinâmicos a partir dos perfis salvos. */
    fun sincronizar(ctx: Context) {
        try {
            val perfis = PerfisJogosStore.listar(ctx)
            val limite = ShortcutManagerCompat.getMaxShortcutCountPerActivity(ctx)
                .coerceAtLeast(0)
            val atalhos = perfis.take(limite).map { perfil ->
                val rotulo = perfil.nome.ifBlank { perfil.pacote }.take(40)
                val intent = Intent(ctx, MainActivity::class.java)
                    .setAction(Intent.ACTION_VIEW)
                    .putExtra("jogo", perfil.pacote)
                    .putExtra("tela", "game_turbo")
                ShortcutInfoCompat.Builder(ctx, PREFIXO + perfil.pacote)
                    .setShortLabel(rotulo)
                    .setLongLabel(rotulo)
                    .setIcon(IconCompat.createWithResource(ctx, R.drawable.ic_game_turbo))
                    .setIntent(intent)
                    .build()
            }
            ShortcutManagerCompat.setDynamicShortcuts(ctx, atalhos)
        } catch (_: Exception) {
            // Sem suporte ao serviço de atalhos — ignora.
        }
    }

    /** Remove todos os atalhos dinâmicos de jogos. */
    fun limpar(ctx: Context) {
        try {
            ShortcutManagerCompat.removeAllDynamicShortcuts(ctx)
        } catch (_: Exception) {
        }
    }
}
