package com.seguraturbo.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.seguraturbo.app.MainActivity
import com.seguraturbo.app.R
import com.seguraturbo.app.util.GerenciadorRam
import com.seguraturbo.app.util.Preferencias

/**
 * Widget da tela inicial: mostra o último score de segurança e abre o app ao toque.
 * Também permite limpar a RAM com um toque.
 */
class SeguroTurboWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, construirViews(context))
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_LIMPAR_RAM) {
            val apps = GerenciadorRam.appsFechaveis(context)
            if (apps.isNotEmpty()) {
                GerenciadorRam.liberar(context, apps.map { it.pacote })
            }
            Toast.makeText(context, context.getString(R.string.widget_limpar_ram_ok), Toast.LENGTH_SHORT).show()
            atualizarTodos(context)
        }
    }

    companion object {

        private const val ACTION_LIMPAR_RAM = "com.seguraturbo.app.widget.LIMPAR_RAM"

        /** Monta as RemoteViews com o último score conhecido. */
        private fun construirViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_seguraturbo)

            val score = Preferencias.ultimoScore(context)
            val texto = if (score >= 0) "$score/100" else context.getString(R.string.widget_sem_dados)
            views.setTextViewText(R.id.widget_score, texto)

            val abrir = Intent(context, MainActivity::class.java)
            val pendingAbrir = PendingIntent.getActivity(
                context,
                0,
                abrir,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_raiz, pendingAbrir)

            val intentLimpar = Intent(context, SeguroTurboWidget::class.java).apply {
                action = ACTION_LIMPAR_RAM
            }
            val pendingLimpar = PendingIntent.getBroadcast(
                context,
                1,
                intentLimpar,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_limpar, pendingLimpar)

            return views
        }

        /** Chamado quando o score muda, para refletir no widget na hora. */
        fun atualizarTodos(context: Context) {
            val mgr = AppWidgetManager.getInstance(context) ?: return
            val componente = ComponentName(context, SeguroTurboWidget::class.java)
            val ids = mgr.getAppWidgetIds(componente)
            if (ids.isNotEmpty()) {
                ids.forEach { id ->
                    mgr.updateAppWidget(id, construirViews(context))
                }
            }
        }
    }
}
