package com.seguraturbo.app.util

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.MessageDigest
import java.util.Locale

class VerificadorSenhasVazadas(private val context: Context) {

    fun verificar(hashSha1Maiuscula: String): Resultado {
        val h = hashSha1Maiuscula.uppercase(Locale.ROOT)
        if (h.length < 40) return Resultado(Encontrado = false, Contagem = 0, Mensagem = "Hash SHA-1 inválido")
        val prefixo = h.substring(0, 5)
        val sufixo = h.substring(5, 40)
        val encontrados = buscarNoDicionario(prefixo).filter { it == sufixo }
        return if (encontrados.isNotEmpty()) {
            Resultado(Encontrado = true, Contagem = encontrados.size, Mensagem = "Hash encontrado em listas locais")
        } else {
            Resultado(Encontrado = false, Contagem = 0, Mensagem = "Não encontrado no dicionário local")
        }
    }

    private fun buscarNoDicionario(prefixo: String): List<String> {
        val lista = mutableListOf<String>()
        try {
            context.assets.open("vazadas.txt").use { input ->
                BufferedReader(InputStreamReader(input)).use { br ->
                    var linha: String?
                    while (br.readLine().also { linha = it } != null) {
                        val l = linha!!.trim()
                        if (l.isEmpty() || l.startsWith("#")) continue
                        if (l.startsWith(prefixo, ignoreCase = true)) {
                            val partes = l.split(":")
                            if (partes.size == 2) {
                                val s = partes[1].uppercase(Locale.ROOT)
                                if (s.length == 35) lista.add(s)
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
        return lista
    }

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

    data class Resultado(
        val Encontrado: Boolean,
        val Contagem: Int,
        val Mensagem: String
    )
}
