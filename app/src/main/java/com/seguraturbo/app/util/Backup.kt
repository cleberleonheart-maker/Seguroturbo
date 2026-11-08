package com.seguraturbo.app.util

import android.content.Context
import org.json.JSONObject
import java.io.InputStream

/**
 * Backup e restauração das preferências e do histórico em um único arquivo JSON.
 * Serve para o usuário guardar/transferir as configurações sem depender da nuvem.
 */
object Backup {

    private const val VERSAO = 1

    /** Tamanho máximo aceito para um arquivo de backup (2 MB). */
    const val TAMANHO_MAXIMO = 2 * 1024 * 1024

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

    /**
     * Lê o backup de um fluxo, respeitando [TAMANHO_MAXIMO]. Arquivos maiores
     * são recusados em vez de carregar tudo na memória. Devolve null se o
     * arquivo for grande demais ou não puder ser lido.
     */
    fun lerLimitado(entrada: InputStream): String? = try {
        entrada.use { fluxo ->
            val buffer = ByteArray(8192)
            val total = java.io.ByteArrayOutputStream()
            var lidos = 0
            var lido = fluxo.read(buffer)
            while (lido > 0 && total.size() <= TAMANHO_MAXIMO) {
                total.write(buffer, 0, lido)
                lido = fluxo.read(buffer)
            }
            val bytes = total.toByteArray()
            if (bytes.size > TAMANHO_MAXIMO) null else bytes.decodeToString()
        }
    } catch (e: Exception) {
        null
    }

    /**
     * Lê e aplica o backup de um fluxo. Devolve false se o arquivo for grande
     * demais, inválido ou não puder ser lido.
     */
    fun aplicarDeFluxo(ctx: Context, entrada: InputStream): Boolean {
        val texto = lerLimitado(entrada) ?: return false
        return aplicar(ctx, texto)
    }
}
