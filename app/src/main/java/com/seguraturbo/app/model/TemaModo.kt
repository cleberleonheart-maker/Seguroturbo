package com.seguraturbo.app.model

/** Modo de tema escolhido pelo usuário: seguir o sistema, fixar claro/escuro ou AMOLED. */
enum class TemaModo {
    SISTEMA,
    CLARO,
    ESCURO,

    /** Escuro com preto puro para economizar bateria em telas OLED. */
    AMOLED
}
