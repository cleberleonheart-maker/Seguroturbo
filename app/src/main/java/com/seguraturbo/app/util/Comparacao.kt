package com.seguraturbo.app.util

import kotlin.math.roundToInt

/**
 * Estatísticas locais dos resultados de benchmark, para comparar a nota atual
 * com o histórico do próprio aparelho. Tudo local, sem servidor.
 */
object Comparacao {

    data class Resumo(
        val media: Int?,
        val melhor: Int?,
        val posicao: Int?,
        val total: Int
    )

    /**
     * Calcula média, melhor nota e a posição (ranking) de [atual] entre as
     * [pontuacoes] (1 = melhor). Se [atual] for nulo, [Resumo.posicao] fica nulo.
     */
    fun resumo(pontuacoes: List<Int>, atual: Int? = null): Resumo {
        val media = if (pontuacoes.isEmpty()) null else pontuacoes.average().roundToInt()
        val melhor = pontuacoes.maxOrNull()
        val posicao = atual?.let { nota -> pontuacoes.count { it > nota } + 1 }
        return Resumo(media = media, melhor = melhor, posicao = posicao, total = pontuacoes.size)
    }
}
