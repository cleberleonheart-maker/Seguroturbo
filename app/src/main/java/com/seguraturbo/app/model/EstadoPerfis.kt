package com.seguraturbo.app.model

import org.json.JSONObject

/**
 * Estado completo dos ajustes que um perfil de uso aplica. Serve para guardar
 * o estado anterior do usuário e devolvê-lo exatamente como estava, em vez de
 * reaplicar um preset inteiro (que sobrescreveria ajustes feitos pelo usuário).
 */
data class EstadoPerfis(
    val perfilUso: PerfilUso,
    val perfilDesempenho: PerfilDesempenho,
    val dndAuto: Boolean,
    val travaTela: Boolean,
    val silenciarMidia: Boolean,
    val lembrete: Boolean,
    val atualizacaoAuto: Boolean
) {

    fun toJson(): String = JSONObject()
        .put("perfilUso", perfilUso.name)
        .put("perfilDesempenho", perfilDesempenho.name)
        .put("dndAuto", dndAuto)
        .put("travaTela", travaTela)
        .put("silenciarMidia", silenciarMidia)
        .put("lembrete", lembrete)
        .put("atualizacaoAuto", atualizacaoAuto)
        .toString()

    companion object {
        fun fromJson(texto: String?): EstadoPerfis? {
            if (texto.isNullOrBlank()) return null
            return try {
                val o = JSONObject(texto)
                EstadoPerfis(
                    perfilUso = runCatching {
                        PerfilUso.valueOf(o.optString("perfilUso"))
                    }.getOrDefault(PerfilUso.PADRAO),
                    perfilDesempenho = runCatching {
                        PerfilDesempenho.valueOf(o.optString("perfilDesempenho"))
                    }.getOrDefault(PerfilDesempenho.EQUILIBRADO),
                    dndAuto = o.optBoolean("dndAuto"),
                    travaTela = o.optBoolean("travaTela"),
                    silenciarMidia = o.optBoolean("silenciarMidia"),
                    lembrete = o.optBoolean("lembrete"),
                    atualizacaoAuto = o.optBoolean("atualizacaoAuto")
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
