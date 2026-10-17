package com.seguraturbo.app.model

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Representa um jogo instalado no dispositivo.
 * O ícone é carregado de verdade a partir do PackageManager.
 */
data class Game(
    val nome: String,
    val pacote: String,
    val icone: ImageBitmap? = null,
    val otimizado: Boolean = false
)
