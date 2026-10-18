package com.seguraturbo.app.util

/**
 * Teste de tela/touch: calcula os números do teste de resposta ao toque.
 *
 * A captura dos toques acontece na interface (Compose); aqui fica só a matemática,
 * para poder ser testada sem dispositivo.
 */
object TesteToque {

    /** Duração padrão do teste, em segundos. */
    const val DURACAO_PADRAO_S = 10

    /** Resultado de um teste de toque. */
    data class Resultado(
        val toques: Int,
        val duracaoMs: Long,
        val respostaMediaMs: Double,
        val maxPontos: Int
    ) {
        /** Toques por segundo. */
        val toquesPorSegundo: Double
            get() = if (duracaoMs <= 0L) 0.0 else toques * 1000.0 / duracaoMs
    }

    /**
     * Monta o resultado a partir das medições brutas.
     * @param intervalosMs tempo entre toques consecutivos (usado na resposta média).
     */
    fun calcular(
        toques: Int,
        duracaoMs: Long,
        intervalosMs: List<Long>,
        maxPontos: Int
    ): Resultado {
        val media = if (intervalosMs.isEmpty()) 0.0 else intervalosMs.average()
        return Resultado(
            toques = toques,
            duracaoMs = duracaoMs,
            respostaMediaMs = media,
            maxPontos = maxPontos.coerceAtLeast(0)
        )
    }
}
