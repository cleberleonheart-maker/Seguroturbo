package com.seguraturbo.app.util

object ReferenciaClasse {
    data class Faixa(
        val classe: String,
        val min: Int,
        val max: Int,
        val descricao: String
    )

    fun faixaPara(pontuacao: Int): Faixa {
        return when {
            pontuacao >= 90000 -> Faixa("Topo", 90000, Int.MAX_VALUE, "Acima da média de flagships atuais")
            pontuacao >= 60000 -> Faixa("Alto", 60000, 89999, "Faixa típica de intermediários altos / alguns topo")
            pontuacao >= 35000 -> Faixa("Médio", 35000, 59999, "Faixa intermediária")
            pontuacao >= 15000 -> Faixa("Básico", 15000, 34999, "Faixa de entrada/intermediário básico")
            else -> Faixa("Muito básico", 0, 14999, "Abaixo da maioria dos aparelhos atuais")
        }
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
