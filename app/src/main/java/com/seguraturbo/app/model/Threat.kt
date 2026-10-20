package com.seguraturbo.app.model

/**
 * Um alerta de segurança encontrado durante a varredura.
 * Os alertas vêm de verificações reais do dispositivo (root, configurações
 * inseguras, cache, acessibilidade etc.). Cada alerta pode trazer uma ação
 * sugerida para o usuário corrigir o problema.
 */
data class Threat(
    val nome: String,
    val tipo: String,
    val risco: String,
    val descricao: String = "",
    val removido: Boolean = false,
    val acao: AcaoConfig = AcaoConfig.NENHUMA,
    /** Peso (0–100) subtraído da pontuação de segurança quando o alerta existe. */
    val peso: Int = 0,
    /** Pacote do app relacionado ao alerta (usado pelas ações que abrem um app). */
    val pacoteApp: String? = null
)

/** Ação sugerida para corrigir um alerta (abre a tela de configuração adequada). */
enum class AcaoConfig {
    NENHUMA,
    FONTES_DESCONHECIDAS,
    OPCOES_DESENVOLVEDOR,
    ACESSIBILIDADE,
    SEGURANCA,
    CACHE,
    BATERIA,
    ATUALIZACAO_SISTEMA,
    REDES,
    DISPLAY,
    APPS
}
