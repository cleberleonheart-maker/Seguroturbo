package com.seguraturbo.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.seguraturbo.app.model.AcaoConfig

/**
 * Centraliza a abertura das telas de configuração do sistema correspondentes às
 * ações sugeridas pelos alertas de segurança.
 */
object AcoesSistema {

    /** Abre a tela de configuração adequada. Retorna true se conseguiu abrir. */
    fun abrirConfiguracao(ctx: Context, acao: AcaoConfig): Boolean {
        val intent = when (acao) {
            AcaoConfig.FONTES_DESCONHECIDAS -> intentFontesDesconhecidas(ctx)
            AcaoConfig.OPCOES_DESENVOLVEDOR -> Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            AcaoConfig.ACESSIBILIDADE -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            AcaoConfig.SEGURANCA -> Intent(Settings.ACTION_SECURITY_SETTINGS)
            AcaoConfig.BATERIA -> Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            AcaoConfig.CACHE, AcaoConfig.NENHUMA -> return false
        }
        return tentarAbrir(ctx, intent)
    }

    /** Abre o app/jogo pelo pacote. Retorna true se conseguiu abrir. */
    fun abrirApp(ctx: Context, pacote: String): Boolean {
        val intent = ctx.packageManager.getLaunchIntentForPackage(pacote) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            ContextCompat.startActivity(ctx, intent, null)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun intentFontesDesconhecidas(ctx: Context): Intent {
        // A partir do Android 8 a permissão é por app; enviamos o pacote do próprio app.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${ctx.packageName}"))
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
    }

    private fun tentarAbrir(ctx: Context, intent: Intent): Boolean {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(ctx, intent, null)
            true
        } catch (e: Exception) {
            // Fallback: abre a tela geral de configurações.
            return try {
                val geral = Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ContextCompat.startActivity(ctx, geral, null)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }
}
