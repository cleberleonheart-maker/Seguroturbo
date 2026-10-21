package com.seguraturbo.app.util

/**
 * Ação disparada por um atalho do ícone (toque longo). Fica pendente até a tela
 * de destino consumi-la, evitando acoplar o MainActivity às telas.
 */
enum class AtalhoAcao {
    NENHUMA,
    ESCANEAR,
    LIMPAR_RAM,
    TESTAR_REDE;

    companion object {
        /** Converte o extra "acao" do atalho no enum correspondente. */
        fun de(chave: String?): AtalhoAcao = when (chave) {
            "escanear" -> ESCANEAR
            "limpar_ram" -> LIMPAR_RAM
            "testar_rede" -> TESTAR_REDE
            else -> NENHUMA
        }
    }
}

/**
 * Guarda a ação pendente de um atalho para a tela consumir depois que ela é
 * composta. Simples e sem persistência: vive só em memória.
 */
object AtalhoPendente {
    var acao: AtalhoAcao = AtalhoAcao.NENHUMA

    /** Consome (e limpa) a ação pendente, se houver. */
    fun consumir(): AtalhoAcao {
        val atual = acao
        acao = AtalhoAcao.NENHUMA
        return atual
    }
}
