package com.seguraturbo.app.util

import com.seguraturbo.app.model.PerfilUso

/**
 * Regras do "Modo Economia automático": quando a bateria cai para
 * [LIMITE_BAIXO]% ou menos, o app assume o perfil [PerfilUso.BATERIA]; quando a
 * carga volta, devolve as configurações exatamente como estavam antes.
 *
 * A lógica é pura (sem Android) para poder ser testada na JVM.
 */
object EconomiaAuto {

    /** Carga da bateria (em %) que dispara o modo economia. */
    const val LIMITE_BAIXO = 20

    /** O que fazer ao avaliar a bateria. */
    enum class Acao {
        /** Nada a fazer (estado já coerente). */
        NADA,

        /** Entrar no modo economia: guardar o estado atual e aplicar o perfil Bateria. */
        APLICAR,

        /** Sair do modo economia: devolver o estado guardado. */
        RESTAURAR,

        /** A bateria recuperou, mas o usuário trocou o perfil no meio do caminho. */
        DESCARTAR
    }

    /**
     * Decide a ação com base na carga atual, se o modo economia já estava ativo
     * e no perfil que está em uso.
     */
    fun decidir(nivel: Int, autoAtiva: Boolean, perfilAtual: PerfilUso): Acao = when {
        nivel <= 0 -> Acao.NADA
        nivel <= LIMITE_BAIXO ->
            if (!autoAtiva && perfilAtual != PerfilUso.BATERIA) Acao.APLICAR else Acao.NADA
        autoAtiva ->
            if (perfilAtual == PerfilUso.BATERIA) Acao.RESTAURAR else Acao.DESCARTAR
        else -> Acao.NADA
    }
}
