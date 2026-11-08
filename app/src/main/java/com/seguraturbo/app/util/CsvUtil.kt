package com.seguraturbo.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilidades de CSV compartilhadas pelos relatórios (histórico, inventário de
 * permissões e relatório completo), para que o escape de campos e o formato de
 * data fiquem iguais em todos os arquivos gerados.
 *
 * O formatador de data é criado a cada chamada: [SimpleDateFormat] não é
 * thread-safe e os relatórios são gerados em segundo plano.
 */
object CsvUtil {

    /** Separador usado nos CSV (abre direto no Excel pt-BR). */
    const val SEPARADOR = ';'

    /**
     * Envolve o campo em aspas e escapa as aspas duplas, conforme a regra do
     * CSV (RFC 4180). Sem isso, um título com ";" ou aspas quebraria as colunas.
     */
    fun campo(valor: String): String = "\"" + valor.replace("\"", "\"\"") + "\""

    /** Data/hora local no formato usado nos relatórios (yyyy-MM-dd HH:mm). */
    fun dataHora(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))

    /** Data/hora local com segundos (yyyy-MM-dd HH:mm:ss). */
    fun dataHoraSegundos(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}
