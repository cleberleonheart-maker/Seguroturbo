package com.seguraturbo.app.util

import android.content.Context
import com.seguraturbo.app.model.PerfilDesempenho
import com.seguraturbo.app.model.PerfilJogo
import org.json.JSONArray
import org.json.JSONObject

object PerfisJogosStore {
    private const val CHAVE = "perfis_jogos_json"

    fun listar(ctx: Context): List<PerfilJogo> {
        val json = Armazenamento.lerTexto(ctx, CHAVE, "[]") ?: "[]"
        val arr = JSONArray(json)
        val lista = mutableListOf<PerfilJogo>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            lista.add(
                PerfilJogo(
                    pacote = o.optString("pacote"),
                    nome = o.optString("nome"),
                    perfil = runCatching { PerfilDesempenho.valueOf(o.optString("perfil", "EQUILIBRADO")) }.getOrDefault(PerfilDesempenho.EQUILIBRADO),
                    naoPerturbe = o.optBoolean("naoPerturbe", false),
                    travaTela = o.optBoolean("travaTela", false),
                    barraJogo = o.optBoolean("barraJogo", true)
                )
            )
        }
        return lista
    }

    fun obter(ctx: Context, pacote: String): PerfilJogo? = listar(ctx).firstOrNull { it.pacote == pacote }

    fun salvar(ctx: Context, perfil: PerfilJogo) {
        val atual = listar(ctx).filter { it.pacote != perfil.pacote }.toMutableList()
        atual.add(perfil)
        val arr = JSONArray()
        atual.forEach {
            arr.put(
                JSONObject().apply {
                    put("pacote", it.pacote)
                    put("nome", it.nome)
                    put("perfil", it.perfil.name)
                    put("naoPerturbe", it.naoPerturbe)
                    put("travaTela", it.travaTela)
                    put("barraJogo", it.barraJogo)
                }
            )
        }
        Armazenamento.gravarTexto(ctx, CHAVE, arr.toString())
    }

    fun remover(ctx: Context, pacote: String) {
        val atual = listar(ctx).filter { it.pacote != pacote }
        val arr = JSONArray()
        atual.forEach {
            arr.put(
                JSONObject().apply {
                    put("pacote", it.pacote)
                    put("nome", it.nome)
                    put("perfil", it.perfil.name)
                    put("naoPerturbe", it.naoPerturbe)
                    put("travaTela", it.travaTela)
                    put("barraJogo", it.barraJogo)
                }
            )
        }
        Armazenamento.gravarTexto(ctx, CHAVE, arr.toString())
    }
}
