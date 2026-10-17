package com.seguraturbo.app.util

import android.content.Context
import com.seguraturbo.app.model.HistoricoEntrada

/**
 * Guarda o histórico de ações (scans, limpezas, benchmarks, turbo) no próprio
 * dispositivo usando SharedPreferences — sem dependências externas (Room/DataStore).
 */
object HistoricoStore {

    private const val PREFS = "seguraturbo_historico"
    private const val CHAVE = "entradas"
    private const val MAXIMO = 20
    private const val SEP = "\u001F"
    private const val QUEBRA = "\n"

    fun listar(ctx: Context): List<HistoricoEntrada> {
        val bruto = prefs(ctx).getString(CHAVE, "") ?: ""
        if (bruto.isBlank()) return emptyList()
        return bruto.split(QUEBRA).mapNotNull { linha ->
            val partes = linha.split(SEP)
            if (partes.size < 4) return@mapNotNull null
            val tipo = runCatching { HistoricoEntrada.Tipo.valueOf(partes[0]) }
                .getOrDefault(HistoricoEntrada.Tipo.SCAN)
            HistoricoEntrada(
                tipo = tipo,
                data = partes[1].toLongOrNull() ?: 0L,
                titulo = partes[2],
                detalhe = partes[3]
            )
        }.sortedByDescending { it.data }
    }

    fun adicionar(ctx: Context, entrada: HistoricoEntrada) {
        val atual = listar(ctx).toMutableList()
        atual.add(0, entrada)
        val limitada = atual.take(MAXIMO)
        val texto = limitada.joinToString(QUEBRA) {
            listOf(it.tipo.name, it.data.toString(), sanitizar(it.titulo), sanitizar(it.detalhe))
                .joinToString(SEP)
        }
        prefs(ctx).edit().putString(CHAVE, texto).apply()
    }

    fun limpar(ctx: Context) {
        prefs(ctx).edit().remove(CHAVE).apply()
    }

    // ---- Backup / restauração ----

    /** Todas as entradas, prontas para serializar em JSON. */
    internal fun paraJson(ctx: Context): org.json.JSONArray =
        entradasParaJson(listar(ctx))

    /** Substitui o histórico pelas entradas vindas do backup. */
    internal fun deJson(ctx: Context, arr: org.json.JSONArray) {
        definir(ctx, entradasDeJson(arr))
    }

    internal fun definir(ctx: Context, lista: List<HistoricoEntrada>) {
        val limitada = lista.sortedByDescending { it.data }.take(MAXIMO)
        prefs(ctx).edit().putString(CHAVE, serializar(limitada)).apply()
    }

    /** Converte as entradas para JSON (função pura, testável sem Android). */
    internal fun entradasParaJson(lista: List<HistoricoEntrada>): org.json.JSONArray {
        val arr = org.json.JSONArray()
        lista.forEach { e ->
            arr.put(
                org.json.JSONObject().apply {
                    put("tipo", e.tipo.name)
                    put("data", e.data)
                    put("titulo", e.titulo)
                    put("detalhe", e.detalhe)
                }
            )
        }
        return arr
    }

    /** Lê as entradas de um JSON (função pura, tolerante a dados incompletos). */
    internal fun entradasDeJson(arr: org.json.JSONArray): List<HistoricoEntrada> =
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            HistoricoEntrada(
                tipo = runCatching { HistoricoEntrada.Tipo.valueOf(arr.optJSONObject(i)!!.optString("tipo")) }
                    .getOrDefault(HistoricoEntrada.Tipo.SCAN),
                data = o.optLong("data"),
                titulo = o.optString("titulo"),
                detalhe = o.optString("detalhe")
            )
        }

    private fun serializar(lista: List<HistoricoEntrada>): String =
        lista.joinToString(QUEBRA) {
            listOf(it.tipo.name, it.data.toString(), sanitizar(it.titulo), sanitizar(it.detalhe))
                .joinToString(SEP)
        }

    private fun sanitizar(texto: String) = texto.replace(SEP, " ").replace(QUEBRA, " ")

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
