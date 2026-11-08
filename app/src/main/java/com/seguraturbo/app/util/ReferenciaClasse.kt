package com.seguraturbo.app.util

/**
 * Classificação do aparelho a partir da pontuação total do benchmark.
 *
 * A pontuação total é a soma das quatro métricas medidas (CPU, GPU, MEM e UX).
 * A escala real do benchmark fica entre ~310.000 (aparelho de entrada) e
 * ~930.000 (topo de linha), então as faixas abaixo acompanham essa escala —
 * usar a escala antiga (0–100.000) colocava todo aparelho em "Topo".
 */
object ReferenciaClasse {

    /** Menor total possível somando o piso de cada métrica. */
    const val TOTAL_MINIMO = 310_000

    /** Maior total possível somando o teto de cada métrica. */
    const val TOTAL_MAXIMO = 930_000

    data class Faixa(
        val classe: String,
        val min: Int,
        val max: Int,
        val descricao: String
    )

    private val FAIXAS = listOf(
        Faixa("Topo", 780_000, TOTAL_MAXIMO, "Faixa de aparelhos topo de linha"),
        Faixa("Alto", 680_000, 779_999, "Faixa típica de intermediários altos"),
        Faixa("Médio", 580_000, 679_999, "Faixa intermediária"),
        Faixa("Básico", 480_000, 579_999, "Faixa de entrada/intermediário básico"),
        Faixa("Muito básico", TOTAL_MINIMO, 479_999, "Abaixo da maioria dos aparelhos atuais")
    )

    fun faixaPara(pontuacao: Int): Faixa {
        val total = pontuacao.coerceIn(TOTAL_MINIMO, TOTAL_MAXIMO)
        return FAIXAS.first { total >= it.min }
    }

    fun resumo(pontuacao: Int): String {
        val f = faixaPara(pontuacao)
        val maxStr = if (f.max == Int.MAX_VALUE) "+" else f.max.toString()
        return "Classe: ${f.classe} • Faixa típica: ${f.min}–$maxStr"
    }

    fun sugestao(pontuacao: Int): String {
        return when (faixaPara(pontuacao).classe) {
            "Topo" -> "Desempenho muito bom para qualquer tipo de jogo"
            "Alto" -> "Bom para a maioria dos jogos, inclusive mais pesados"
            "Médio" -> "Suficiente para jogos leves/médios na maioria dos casos"
            "Básico" -> "Indicado para apps e jogos mais leves"
            else -> "Recomendado priorizar apps leves"
        }
    }
}
