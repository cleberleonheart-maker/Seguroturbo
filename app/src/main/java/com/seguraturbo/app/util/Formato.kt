package com.seguraturbo.app.util

import java.util.Locale

/** Formatação de tamanhos e datas usada em várias telas. */
object Formato {

    // Reaproveita o formatador (criá-lo a cada chamada era custoso na lista de histórico).
    // Uso restrito à thread principal (composição), então não há risco de concorrência.
    private val formatoData =
        java.text.SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())

    fun tamanho(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1 -> String.format(Locale.getDefault(), "%.2f GB", gb)
            mb >= 1 -> String.format(Locale.getDefault(), "%.1f MB", mb)
            kb >= 1 -> String.format(Locale.getDefault(), "%.0f KB", kb)
            else -> "$bytes B"
        }
    }

    fun data(timestamp: Long): String {
        return formatoData.format(java.util.Date(timestamp))
    }
}
