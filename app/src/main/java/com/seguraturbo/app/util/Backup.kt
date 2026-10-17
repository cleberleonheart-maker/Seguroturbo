package com.seguraturbo.app.util

import android.content.Context
import org.json.JSONObject

/**
 * Backup e restauração das preferências e do histórico em um único arquivo JSON.
 * Serve para o usuário guardar/transferir as configurações sem depender da nuvem.
 */
object Backup {

    private const val VERSAO = 1

    /** Gera o conteúdo JSON do backup. */
    fun montar(ctx: Context): String {
        val obj = JSONObject()
        obj.put("app", "SeguroTurbo")
        obj.put("versao", VERSAO)
        obj.put("preferencias", Preferencias.paraJson(ctx))
        obj.put("historico", HistoricoStore.paraJson(ctx))
        return obj.toString(2)
    }

    /**
     * Aplica um backup gerado por [montar]. Devolve false se o texto não for um
     * backup válido (assim não sobrescreve nada por engano).
     */
    fun aplicar(ctx: Context, texto: String): Boolean = try {
        val obj = JSONObject(texto)
        if (obj.optInt("versao", 0) <= 0) {
            false
        } else {
            obj.optJSONObject("preferencias")?.let { Preferencias.deJson(ctx, it) }
            obj.optJSONArray("historico")?.let { HistoricoStore.deJson(ctx, it) }
            true
        }
    } catch (e: Exception) {
        false
    }
}
