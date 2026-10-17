package com.seguraturbo.app.util

import android.content.Context
import java.io.File

/**
 * Calcula e remove o cache real do app (diretórios cacheDir / externalCacheDir).
 * Diferente de antes, o "lixo" agora é medido de verdade no dispositivo.
 */
object StorageCleaner {

    fun calcularCacheBytes(ctx: Context): Long {
        var total = 0L
        ctx.cacheDir?.let { total += tamanhoRecursivo(it) }
        ctx.externalCacheDir?.let { total += tamanhoRecursivo(it) }
        return total
    }

    fun limparCache(ctx: Context): Long {
        var liberado = 0L
        ctx.cacheDir?.let { liberado += apagarConteudo(it) }
        ctx.externalCacheDir?.let { liberado += apagarConteudo(it) }
        return liberado
    }

    private fun tamanhoRecursivo(arquivo: File): Long {
        if (!arquivo.exists()) return 0L
        if (arquivo.isFile) return arquivo.length()
        var soma = 0L
        arquivo.listFiles()?.forEach { soma += tamanhoRecursivo(it) }
        return soma
    }

    private fun apagarConteudo(dir: File): Long {
        var liberado = 0L
        dir.listFiles()?.forEach { filho ->
            val tamanho = tamanhoRecursivo(filho)
            if (apagar(filho)) liberado += tamanho
        }
        return liberado
    }

    private fun apagar(arquivo: File): Boolean {
        if (arquivo.isDirectory) {
            arquivo.listFiles()?.forEach { apagar(it) }
        }
        return arquivo.delete()
    }
}
