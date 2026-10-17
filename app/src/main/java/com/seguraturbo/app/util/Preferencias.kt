package com.seguraturbo.app.util

import android.content.Context
import com.seguraturbo.app.model.PerfilDesempenho
import com.seguraturbo.app.model.TemaModo

/**
 * Preferências simples do app (tema, tamanho da fonte e jogos otimizados),
 * persistidas em SharedPreferences.
 */
object Preferencias {

    private const val PREFS = "seguraturbo_prefs"
    private const val K_TEMA_ESCURO = "tema_escuro"
    private const val K_TEMA_MODO = "tema_modo"
    private const val K_ESCALA_FONTE = "escala_fonte"
    private const val K_JOGOS_OTIMIZADOS = "jogos_otimizados"
    private const val K_LEMBRETE = "lembrete_ativo"
    private const val K_ULTIMO_SCORE = "ultimo_score"
    private const val K_VERSAO_IGNORADA = "versao_ignorada"
    private const val K_DND_AUTO = "dnd_auto"
    private const val K_FILTRO_ANTERIOR = "filtro_anterior"
    private const val K_PERFIL_DESEMPENHO = "perfil_desempenho"
    private const val K_ONBOARDING_VISTO = "onboarding_visto"

    // ---- Tema ----
    fun temaEscuro(ctx: Context): Boolean =
        prefs(ctx).getBoolean(K_TEMA_ESCURO, true)

    fun definirTemaEscuro(ctx: Context, escuro: Boolean) {
        prefs(ctx).edit().putBoolean(K_TEMA_ESCURO, escuro).apply()
    }

    /**
     * Modo de tema. Se ainda não foi escolhido, deriva do antigo booleano
     * claro/escuro para preservar a preferência de quem já usava o app.
     */
    fun temaModo(ctx: Context): TemaModo {
        val salvo = prefs(ctx).getString(K_TEMA_MODO, null)
        if (salvo != null) {
            runCatching { TemaModo.valueOf(salvo) }.getOrNull()?.let { return it }
        }
        return if (temaEscuro(ctx)) TemaModo.ESCURO else TemaModo.CLARO
    }

    fun definirTemaModo(ctx: Context, modo: TemaModo) {
        prefs(ctx).edit().putString(K_TEMA_MODO, modo.name).apply()
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

    // ---- Atualizações (versão que o usuário escolheu ignorar) ----
    fun versaoIgnorada(ctx: Context): String =
        prefs(ctx).getString(K_VERSAO_IGNORADA, "") ?: ""

    fun definirVersaoIgnorada(ctx: Context, versao: String) {
        prefs(ctx).edit().putString(K_VERSAO_IGNORADA, versao).apply()
    }

    // ---- Não Perturbe automático no Game Turbo ----
    fun dndAuto(ctx: Context): Boolean =
        prefs(ctx).getBoolean(K_DND_AUTO, false)

    fun definirDndAuto(ctx: Context, ativo: Boolean) {
        prefs(ctx).edit().putBoolean(K_DND_AUTO, ativo).apply()
    }

    /** Modo de interrupção do sistema antes de o SeguroTurbo ativar o Não Perturbe. */
    fun filtroAnterior(ctx: Context): Int =
        prefs(ctx).getInt(K_FILTRO_ANTERIOR, 1) // 1 = INTERRUPTION_FILTER_ALL

    fun definirFiltroAnterior(ctx: Context, filtro: Int) {
        prefs(ctx).edit().putInt(K_FILTRO_ANTERIOR, filtro).apply()
    }

    // ---- Perfil de desempenho do Game Turbo ----
    fun perfilDesempenho(ctx: Context): PerfilDesempenho = runCatching {
        PerfilDesempenho.valueOf(
            prefs(ctx).getString(K_PERFIL_DESEMPENHO, null) ?: PerfilDesempenho.EQUILIBRADO.name
        )
    }.getOrDefault(PerfilDesempenho.EQUILIBRADO)

    fun definirPerfilDesempenho(ctx: Context, perfil: PerfilDesempenho) {
        prefs(ctx).edit().putString(K_PERFIL_DESEMPENHO, perfil.name).apply()
    }

    // ---- Onboarding (tela de boas-vindas exibida só no primeiro uso) ----
    fun onboardingVisto(ctx: Context): Boolean =
        prefs(ctx).getBoolean(K_ONBOARDING_VISTO, false)

    fun definirOnboardingVisto(ctx: Context, visto: Boolean) {
        prefs(ctx).edit().putBoolean(K_ONBOARDING_VISTO, visto).apply()
    }

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
