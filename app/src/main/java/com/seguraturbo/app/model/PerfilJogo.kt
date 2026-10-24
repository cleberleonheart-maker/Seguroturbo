package com.seguraturbo.app.model

data class PerfilJogo(
    val pacote: String,
    val nome: String = "",
    val perfil: PerfilDesempenho = PerfilDesempenho.EQUILIBRADO,
    val naoPerturbe: Boolean = false,
    val travaTela: Boolean = false,
    val barraJogo: Boolean = true
)
