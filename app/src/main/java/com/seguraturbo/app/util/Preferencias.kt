package com.seguraturbo.app.util

import android.content.Context

/**
 * Preferências simples do app (tema, tamanho da fonte e jogos otimizados),
 * persistidas em SharedPreferences.
 */
object Preferencias {

    private const val PREFS = "seguraturbo_prefs"
    private const val K_TEMA_ESCURO = "tema_escuro"
    private const val K_ESCALA_FONTE = "escala_fonte"
    private const val K_JOGOS_OTIMIZADOS = "jogos_otimizados"
    private const val K_LEMBRETE = "lembrete_ativo"
    private const val K_ULTIMO_SCORE = "ultimo_score"

    // ---- Tema ----
    fun temaEscuro(ctx: Context): Boolean =
        prefs(ctx).getBoolean(K_TEMA_ESCURO, true)

    fun definirTemaEscuro(ctx: Context, escuro: Boolean) {
        prefs(ctx).edit().putBoolean(K_TEMA_ESCURO, escuro).apply()
    }

    // ---- Tamanho da fonte (1f = padrão) ----
    fun escalaFonte(ctx: Context): Float =
        prefs(ctx).getFloat(K_ESCALA_FONTE, 1f)

    fun definirEscalaFonte(ctx: Context, escala: Float) {
        prefs(ctx).edit().putFloat(K_ESCALA_FONTE, escala).apply()
    }

    // ---- Jogos otimizados (pacotes) ----
    fun jogosOtimizados(ctx: Context): Set<String> =
        prefs(ctx).getStringSet(K_JOGOS_OTIMIZADOS, emptySet()) ?: emptySet()

    fun adicionarJogosOtimizados(ctx: Context, pacotes: Collection<String>) {
        val atual = jogosOtimizados(ctx).toMutableSet()
        atual.addAll(pacotes)
        prefs(ctx).edit().putStringSet(K_JOGOS_OTIMIZADOS, atual).apply()
    }

    // ---- Lembrete de verificação (notificação) ----
    fun lembreteAtivo(ctx: Context): Boolean =
        prefs(ctx).getBoolean(K_LEMBRETE, false)

    fun definirLembreteAtivo(ctx: Context, ativo: Boolean) {
        prefs(ctx).edit().putBoolean(K_LEMBRETE, ativo).apply()
    }

    // ---- Último score de segurança (usado pelo widget) ----
    /** Retorna o último score (0–100) ou -1 se nunca escaneou. */
    fun ultimoScore(ctx: Context): Int =
        prefs(ctx).getInt(K_ULTIMO_SCORE, -1)

    fun definirUltimoScore(ctx: Context, score: Int) {
        prefs(ctx).edit().putInt(K_ULTIMO_SCORE, score).apply()
    }

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
