package com.seguraturbo.app.util

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.MessageDigest
import java.util.Locale

/**
 * Verificador de senhas vazadas 100% offline.
 *
 * A senha nunca sai do aparelho: o app calcula o SHA-1 localmente e compara
 * apenas com o dicionário embaralhado que vem em `assets/vazadas.txt`
 * (formato `PREFIXO:SUFIXO`, no mesmo esquema de k-anonymity usado pelo
 * serviço público de consulta — aqui, 100% local).
 */
class VerificadorSenhasVazadas(private val context: Context) {

    /** Motivo do resultado, para que a tela possa traduzir a mensagem. */
    enum class Status {
        /** A senha consta no dicionário local. */
        VAZADA,

        /** A senha não consta no dicionário local. */
        SEGURA,

        /** O hash informado não tem o formato esperado. */
        HASH_INVALIDO,

        /** O dicionário não pôde ser lido. */
        DICIONARIO_INDISPONIVEL
    }

    data class Resultado(
        val Encontrado: Boolean,
        val Contagem: Int,
        val Status: Status
    )

    /**
     * Dicionário carregado uma única vez: prefixo -> quantidade de senhas com
     * esse prefixo (o grupo de k-anonymity).
     */
    private val dicionario: Map<String, Int> by lazy { carregarDicionario() }

    /** Verdadeiro quando o dicionário local está disponível. */
    fun dicionarioDisponivel(): Boolean = dicionario.isNotEmpty()

    /** Quantas senhas o dicionário local conhece. */
    fun totalNoDicionario(): Int = dicionario.values.sum()

    fun verificar(hashSha1Maiuscula: String): Resultado {
        val h = hashSha1Maiuscula.uppercase(Locale.ROOT)
        if (!formatoValido(h)) {
            return Resultado(Encontrado = false, Contagem = 0, Status = Status.HASH_INVALIDO)
        }
        if (dicionario.isEmpty()) {
            return Resultado(Encontrado = false, Contagem = 0, Status = Status.DICIONARIO_INDISPONIVEL)
        }
        val prefixo = h.substring(0, 5)
        val sufixo = h.substring(5, 40)
        val encontrados = sufixosPorPrefixo[prefixo].orEmpty().filter { it == sufixo }
        return if (encontrados.isNotEmpty()) {
            Resultado(
                Encontrado = true,
                Contagem = dicionario[prefixo] ?: encontrados.size,
                Status = Status.VAZADA
            )
        } else {
            Resultado(
                Encontrado = false,
                Contagem = dicionario[prefixo] ?: 0,
                Status = Status.SEGURA
            )
        }
    }

    /** Sufixos guardados por prefixo, calculados uma vez a partir do asset. */
    private val sufixosPorPrefixo: Map<String, List<String>> by lazy {
        parseDicionario(linhasDoAsset())
    }

    private fun linhasDoAsset(): List<String> = try {
        context.assets.open("vazadas.txt").use { input ->
            BufferedReader(InputStreamReader(input)).readLines()
        }
    } catch (_: Exception) {
        emptyList()
    }

    private fun carregarDicionario(): Map<String, Int> =
        sufixosPorPrefixo.mapValues { (_, lista) -> lista.size }

    companion object {

        /** SHA-1 em hexadecimal maiúsculo (40 caracteres). */
        fun sha1(str: String): String {
            val md = MessageDigest.getInstance("SHA-1")
            val bytes = md.digest(str.toByteArray(Charsets.UTF_8))
            val sb = StringBuilder(40)
            for (b in bytes) {
                val v = b.toInt() and 0xff
                if (v < 16) sb.append('0')
                sb.append(v.toString(16))
            }
            return sb.toString().uppercase(Locale.ROOT)
        }

        /** O hash tem o formato esperado (40 dígitos hexadecimais)? */
        fun formatoValido(hash: String): Boolean =
            hash.length == 40 && hash.all { it in '0'..'9' || it in 'A'..'F' }

        /**
         * Converte as linhas do dicionário (`PREFIXO:SUFIXO`) em um mapa de prefixo
         * para sufixos. Função pura, usada nos testes.
         */
        fun parseDicionario(linhas: List<String>): Map<String, List<String>> {
            val mapa = mutableMapOf<String, MutableList<String>>()
            linhas.forEach { bruta ->
                val l = bruta.trim()
                if (l.isEmpty() || l.startsWith("#")) return@forEach
                val partes = l.split(":")
                if (partes.size != 2) return@forEach
                val prefixo = partes[0].uppercase(Locale.ROOT)
                val sufixo = partes[1].uppercase(Locale.ROOT)
                if (prefixo.length != 5 || sufixo.length != 35) return@forEach
                if (!prefixo.all { it in '0'..'9' || it in 'A'..'F' }) return@forEach
                if (!sufixo.all { it in '0'..'9' || it in 'A'..'F' }) return@forEach
                mapa.getOrPut(prefixo) { mutableListOf() }.add(sufixo)
            }
            return mapa
        }
    }
}
