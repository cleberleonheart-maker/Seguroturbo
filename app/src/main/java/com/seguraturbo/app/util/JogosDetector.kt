package com.seguraturbo.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.seguraturbo.app.model.Game

/**
 * Detecta jogos REAIS instalados no dispositivo.
 *
 * Estratégia: listamos os apps com atividade de launcher e filtramos os que o
 * próprio sistema classificou como jogo — pela flag android:isGame
 * (ApplicationInfo.FLAG_IS_GAME, desde a API 1) ou pela categoria CATEGORY_GAME (API 26+).
 * Não existe Intent.CATEGORY_GAME — a classificação de "jogo" fica no app.
 */
object JogosDetector {

    fun detectar(ctx: Context): List<Game> {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolvidos = try {
            pm.queryIntentActivities(intent, 0)
        } catch (e: Exception) {
            emptyList()
        }

        return resolvidos
            .filter { ehJogo(it) }
            .mapNotNull { info ->
                val pacote = info.activityInfo?.packageName ?: return@mapNotNull null
                val nome = info.loadLabel(pm).toString()
                Game(nome = nome, pacote = pacote, icone = carregarIcone(pm, info))
            }
            .distinctBy { it.pacote }
            .sortedBy { it.nome.lowercase() }
    }

    @Suppress("DEPRECATION")
    private fun ehJogo(info: ResolveInfo): Boolean {
        val app = info.activityInfo?.applicationInfo ?: return false
        // android:isGame / FLAG_IS_GAME existe desde a API 1 — cobre o Android 7.
        if ((app.flags and ApplicationInfo.FLAG_IS_GAME) != 0) return true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return app.category == ApplicationInfo.CATEGORY_GAME
        }
        return false
    }

    private fun carregarIcone(pm: PackageManager, info: ResolveInfo): ImageBitmap? = try {
        info.loadIcon(pm).toBitmap(96, 96).asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
