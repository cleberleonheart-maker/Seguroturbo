package com.seguraturbo.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.seguraturbo.app.MainActivity
import com.seguraturbo.app.R
import com.seguraturbo.app.util.Preferencias

/**
 * Widget da tela inicial: mostra o último score de segurança e abre o app ao toque.
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

    companion object {

        /** Monta as RemoteViews com o último score conhecido. */
        private fun construirViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_seguraturbo)

            val score = Preferencias.ultimoScore(context)
            val texto = if (score >= 0) "$score/100" else context.getString(R.string.widget_sem_dados)
            views.setTextViewText(R.id.widget_score, texto)

            val abrir = Intent(context, MainActivity::class.java)
            val pending = PendingIntent.getActivity(
                context,
                0,
                abrir,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_raiz, pending)
            return views
        }

        /** Chamado quando o score muda, para refletir no widget na hora. */
        fun atualizarTodos(context: Context) {
            val mgr = AppWidgetManager.getInstance(context) ?: return
            val componente = ComponentName(context, SeguroTurboWidget::class.java)
            val ids = mgr.getAppWidgetIds(componente)
            ids.forEach { id ->
                mgr.updateAppWidget(id, construirViews(context))
            }
        }
    }
}
