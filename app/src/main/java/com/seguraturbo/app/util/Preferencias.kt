package com.seguraturbo.app.util

import android.content.Context
import com.seguraturbo.app.model.PerfilDesempenho
import com.seguraturbo.app.model.PerfilUso
import com.seguraturbo.app.model.TemaModo

/**
 * Preferências simples do app (tema, tamanho da fonte e jogos otimizados),
 * persistidas em DataStore (com migração automática das SharedPreferences antigas).
 */
object Preferencias {

    private const val K_TEMA_ESCURO = "tema_escuro"
    private const val K_TEMA_MODO = "tema_modo"
    private const val K_ESCALA_FONTE = "escala_fonte"
    private const val K_JOGOS_OTIMIZADOS = "jogos_otimizados"
    private const val K_LEMBRETE = "lembrete_ativo"
    private const val K_ULTIMO_SCORE = "ultimo_score"
    private const val K_VERSAO_IGNORADA = "versao_ignorada"
    private const val K_DND_AUTO = "dnd_auto"
    private const val K_TRAVA_TELA_JOGO = "trava_tela_jogo"
    private const val K_SILENCIAR_MIDIA = "silenciar_midia_jogo"
    private const val K_VOLUME_MIDIA = "volume_midia_anterior"
    private const val K_FILTRO_ANTERIOR = "filtro_anterior"
    private const val K_PERFIL_DESEMPENHO = "perfil_desempenho"
    private const val K_PERFIL_USO = "perfil_uso"
    private const val K_ONBOARDING_VISTO = "onboarding_visto"
    private const val K_ATUALIZACAO_AUTO = "atualizacao_automatica"
    private const val K_ULTIMA_VERSAO_VISTA = "ultima_versao_vista"
    private const val K_AUTO_LIMPAR_RAM_JOGO = "auto_limpar_ram_jogo"
    private const val K_SESSAO_FOCO_ATIVO = "sessao_foco_ativo"
    private const val K_SESSAO_FOCO_FIM = "sessao_foco_fim"

    // ---- Tema ----
    fun temaEscuro(ctx: Context): Boolean =
        Armazenamento.lerBooleano(ctx, K_TEMA_ESCURO, true)

    fun definirTemaEscuro(ctx: Context, escuro: Boolean) {
        Armazenamento.gravarBooleano(ctx, K_TEMA_ESCURO, escuro)
    }

    /**
     * Modo de tema. Se ainda não foi escolhido, deriva do antigo booleano
     * claro/escuro para preservar a preferência de quem já usava o app.
     */
    fun temaModo(ctx: Context): TemaModo {
        val salvo = Armazenamento.lerTexto(ctx, K_TEMA_MODO, null)
        if (salvo != null) {
            runCatching { TemaModo.valueOf(salvo) }.getOrNull()?.let { return it }
        }
        return if (temaEscuro(ctx)) TemaModo.ESCURO else TemaModo.CLARO
    }

    fun definirTemaModo(ctx: Context, modo: TemaModo) {
        Armazenamento.gravarTexto(ctx, K_TEMA_MODO, modo.name)
    }

    // ---- Tamanho da fonte (1f = padrão) ----
    fun escalaFonte(ctx: Context): Float =
        Armazenamento.lerFloat(ctx, K_ESCALA_FONTE, 1f)

    fun definirEscalaFonte(ctx: Context, escala: Float) {
        Armazenamento.gravarFloat(ctx, K_ESCALA_FONTE, escala)
    }

    // ---- Jogos otimizados (pacotes) ----
    fun jogosOtimizados(ctx: Context): Set<String> =
        Armazenamento.lerConjunto(ctx, K_JOGOS_OTIMIZADOS, emptySet())

    fun adicionarJogosOtimizados(ctx: Context, pacotes: Collection<String>) {
        val atual = jogosOtimizados(ctx).toMutableSet()
        atual.addAll(pacotes)
        Armazenamento.gravarConjunto(ctx, K_JOGOS_OTIMIZADOS, atual)
    }

    // ---- Lembrete de verificação (notificação) ----
    fun lembreteAtivo(ctx: Context): Boolean =
        Armazenamento.lerBooleano(ctx, K_LEMBRETE, false)

    fun definirLembreteAtivo(ctx: Context, ativo: Boolean) {
        Armazenamento.gravarBooleano(ctx, K_LEMBRETE, ativo)
    }

    // ---- Último score de segurança (usado pelo widget) ----
    /** Retorna o último score (0–100) ou -1 se nunca escaneou. */
    fun ultimoScore(ctx: Context): Int =
        Armazenamento.lerInteiro(ctx, K_ULTIMO_SCORE, -1)

    fun definirUltimoScore(ctx: Context, score: Int) {
        Armazenamento.gravarInteiro(ctx, K_ULTIMO_SCORE, score)
    }

    // ---- Atualizações (versão que o usuário escolheu ignorar) ----
    fun versaoIgnorada(ctx: Context): String =
        Armazenamento.lerTexto(ctx, K_VERSAO_IGNORADA, "") ?: ""

    fun definirVersaoIgnorada(ctx: Context, versao: String) {
        Armazenamento.gravarTexto(ctx, K_VERSAO_IGNORADA, versao)
    }

    // ---- Não Perturbe automático no Game Turbo ----
    fun dndAuto(ctx: Context): Boolean =
        Armazenamento.lerBooleano(ctx, K_DND_AUTO, false)

    fun definirDndAuto(ctx: Context, ativo: Boolean) {
        Armazenamento.gravarBooleano(ctx, K_DND_AUTO, ativo)
    }

    // ---- Bloqueio de tela (screen pinning) ao abrir um jogo ----
    fun travaTelaJogo(ctx: Context): Boolean =
        Armazenamento.lerBooleano(ctx, K_TRAVA_TELA_JOGO, false)

    fun definirTravaTelaJogo(ctx: Context, ativo: Boolean) {
        Armazenamento.gravarBooleano(ctx, K_TRAVA_TELA_JOGO, ativo)
    }

    // ---- Silenciar só mídia no Modo Jogo ----
    fun silenciarMidia(ctx: Context): Boolean =
        Armazenamento.lerBooleano(ctx, K_SILENCIAR_MIDIA, false)

    fun definirSilenciarMidia(ctx: Context, ativo: Boolean) {
        Armazenamento.gravarBooleano(ctx, K_SILENCIAR_MIDIA, ativo)
    }

    // ---- Auto-limpeza de RAM ao abrir o jogo ----
    fun autoLimparRamJogo(ctx: Context): Boolean =
        Armazenamento.lerBooleano(ctx, K_AUTO_LIMPAR_RAM_JOGO, false)

    fun definirAutoLimparRamJogo(ctx: Context, ativo: Boolean) {
        Armazenamento.gravarBooleano(ctx, K_AUTO_LIMPAR_RAM_JOGO, ativo)
    }

    // ---- Sessão de foco (Pomodoro) ----
    fun sessaoFocoAtiva(ctx: Context): Boolean =
        Armazenamento.lerBooleano(ctx, K_SESSAO_FOCO_ATIVO, false)

    fun definirSessaoFocoAtiva(ctx: Context, ativo: Boolean) {
        Armazenamento.gravarBooleano(ctx, K_SESSAO_FOCO_ATIVO, ativo)
    }

    fun sessaoFocoFim(ctx: Context): Long =
        Armazenamento.lerLongo(ctx, K_SESSAO_FOCO_FIM, 0L)

    fun definirSessaoFocoFim(ctx: Context, fim: Long) {
        Armazenamento.gravarLongo(ctx, K_SESSAO_FOCO_FIM, fim)
    }

    /** Volume de mídia guardado antes de silenciar (-1 = nenhum). */
    fun volumeMidia(ctx: Context): Int =
        Armazenamento.lerInteiro(ctx, K_VOLUME_MIDIA, -1)

    fun definirVolumeMidia(ctx: Context, volume: Int) {
        Armazenamento.gravarInteiro(ctx, K_VOLUME_MIDIA, volume)
    }

    /** Modo de interrupção do sistema antes de o SeguroTurbo ativar o Não Perturbe. */
    fun filtroAnterior(ctx: Context): Int =
        Armazenamento.lerInteiro(ctx, K_FILTRO_ANTERIOR, 1) // 1 = INTERRUPTION_FILTER_ALL

    fun definirFiltroAnterior(ctx: Context, filtro: Int) {
        Armazenamento.gravarInteiro(ctx, K_FILTRO_ANTERIOR, filtro)
    }

    // ---- Perfil de desempenho do Game Turbo ----
    fun perfilDesempenho(ctx: Context): PerfilDesempenho = runCatching {
        PerfilDesempenho.valueOf(
            Armazenamento.lerTexto(ctx, K_PERFIL_DESEMPENHO, PerfilDesempenho.EQUILIBRADO.name)
                ?: PerfilDesempenho.EQUILIBRADO.name
        )
    }.getOrDefault(PerfilDesempenho.EQUILIBRADO)

    fun definirPerfilDesempenho(ctx: Context, perfil: PerfilDesempenho) {
        Armazenamento.gravarTexto(ctx, K_PERFIL_DESEMPENHO, perfil.name)
    }

    // ---- Perfil de uso (preset que aplica várias preferências) ----
    fun perfilUso(ctx: Context): PerfilUso = runCatching {
        PerfilUso.valueOf(
            Armazenamento.lerTexto(ctx, K_PERFIL_USO, PerfilUso.PADRAO.name)
                ?: PerfilUso.PADRAO.name
        )
    }.getOrDefault(PerfilUso.PADRAO)

    fun definirPerfilUso(ctx: Context, perfil: PerfilUso) {
        Armazenamento.gravarTexto(ctx, K_PERFIL_USO, perfil.name)
    }

    // ---- Onboarding (tela de boas-vindas exibida só no primeiro uso) ----
    fun onboardingVisto(ctx: Context): Boolean =
        Armazenamento.lerBooleano(ctx, K_ONBOARDING_VISTO, false)

    fun definirOnboardingVisto(ctx: Context, visto: Boolean) {
        Armazenamento.gravarBooleano(ctx, K_ONBOARDING_VISTO, visto)
    }

    // ---- Checagem automática de atualizações (em segundo plano) ----
    fun atualizacaoAutomatica(ctx: Context): Boolean =
        Armazenamento.lerBooleano(ctx, K_ATUALIZACAO_AUTO, false)

    fun definirAtualizacaoAutomatica(ctx: Context, ativo: Boolean) {
        Armazenamento.gravarBooleano(ctx, K_ATUALIZACAO_AUTO, ativo)
    }

    // ---- Última versão cujo changelog já foi mostrado (novidades "ao atualizar") ----
    fun ultimaVersaoVista(ctx: Context): String =
        Armazenamento.lerTexto(ctx, K_ULTIMA_VERSAO_VISTA, "") ?: ""

    fun definirUltimaVersaoVista(ctx: Context, versao: String) {
        Armazenamento.gravarTexto(ctx, K_ULTIMA_VERSAO_VISTA, versao)
    }

    // ---- Backup / restauração ----
    /** Todas as preferências, prontas para serializar em JSON. */
    internal fun paraJson(ctx: Context): org.json.JSONObject {
        val obj = org.json.JSONObject()
        Armazenamento.todos(ctx).forEach { (chave, valor) ->
            when (valor) {
                is Set<*> -> obj.put(chave, org.json.JSONArray(valor.toList()))
                else -> obj.put(chave, valor)
            }
        }
        return obj
    }

    /** Restaura as preferências a partir do JSON (mantém o que não vier no arquivo). */
    internal fun deJson(ctx: Context, obj: org.json.JSONObject) {
        obj.keys().forEach { chave ->
            when (val valor = obj.get(chave)) {
                is Boolean -> Armazenamento.gravarBooleano(ctx, chave, valor)
                is Int -> Armazenamento.gravarInteiro(ctx, chave, valor)
                is Long -> Armazenamento.gravarLongo(ctx, chave, valor)
                is Float -> Armazenamento.gravarFloat(ctx, chave, valor)
                is Double -> Armazenamento.gravarFloat(ctx, chave, valor.toFloat())
                is String -> Armazenamento.gravarTexto(ctx, chave, valor)
                is org.json.JSONArray -> {
                    val conjunto = mutableSetOf<String>()
                    for (i in 0 until valor.length()) conjunto.add(valor.getString(i))
                    Armazenamento.gravarConjunto(ctx, chave, conjunto)
                }
            }
        }
    }

    // ---- Turbo agendado ----
    private const val K_TURBO_AGENDADO_ATIVO = "turbo_agendado_ativo"
    private const val K_TURBO_AGENDADO_HORA_LIGAR = "turbo_agendado_hora_ligar"
    private const val K_TURBO_AGENDADO_MIN_LIGAR = "turbo_agendado_min_ligar"
    private const val K_TURBO_AGENDADO_HORA_DESLIGAR = "turbo_agendado_hora_desligar"
    private const val K_TURBO_AGENDADO_MIN_DESLIGAR = "turbo_agendado_min_desligar"

    fun turboAgendadoAtivo(ctx: Context): Boolean =
        Armazenamento.lerBooleano(ctx, K_TURBO_AGENDADO_ATIVO, false)

    fun definirTurboAgendadoAtivo(ctx: Context, ativo: Boolean) {
        Armazenamento.gravarBooleano(ctx, K_TURBO_AGENDADO_ATIVO, ativo)
        if (ativo) TurboAgendador.agendar(ctx) else TurboAgendador.cancelar(ctx)
    }

    fun turboAgendadoHoraLigar(ctx: Context): Int =
        Armazenamento.lerInteiro(ctx, K_TURBO_AGENDADO_HORA_LIGAR, 18)

    fun definirTurboAgendadoHoraLigar(ctx: Context, hora: Int) {
        Armazenamento.gravarInteiro(ctx, K_TURBO_AGENDADO_HORA_LIGAR, hora)
        if (turboAgendadoAtivo(ctx)) TurboAgendador.agendar(ctx)
    }

    fun turboAgendadoMinLigar(ctx: Context): Int =
        Armazenamento.lerInteiro(ctx, K_TURBO_AGENDADO_MIN_LIGAR, 0)

    fun definirTurboAgendadoMinLigar(ctx: Context, min: Int) {
        Armazenamento.gravarInteiro(ctx, K_TURBO_AGENDADO_MIN_LIGAR, min)
        if (turboAgendadoAtivo(ctx)) TurboAgendador.agendar(ctx)
    }

    fun turboAgendadoHoraDesligar(ctx: Context): Int =
        Armazenamento.lerInteiro(ctx, K_TURBO_AGENDADO_HORA_DESLIGAR, 23)

    fun definirTurboAgendadoHoraDesligar(ctx: Context, hora: Int) {
        Armazenamento.gravarInteiro(ctx, K_TURBO_AGENDADO_HORA_DESLIGAR, hora)
        if (turboAgendadoAtivo(ctx)) TurboAgendador.agendar(ctx)
    }

    fun turboAgendadoMinDesligar(ctx: Context): Int =
        Armazenamento.lerInteiro(ctx, K_TURBO_AGENDADO_MIN_DESLIGAR, 0)

    fun definirTurboAgendadoMinDesligar(ctx: Context, min: Int) {
        Armazenamento.gravarInteiro(ctx, K_TURBO_AGENDADO_MIN_DESLIGAR, min)
        if (turboAgendadoAtivo(ctx)) TurboAgendador.agendar(ctx)
    }

    // ---- Histórico de limpezas de RAM ----
    private const val K_HIST_LIMPEZAS = "hist_limpezas_ram"

    /** Lista de eventos de limpeza (timestamp + qtd apps). */
    fun historicoLimpezas(ctx: Context): List<Pair<Long, Int>> {
        val bruto = Armazenamento.lerTexto(ctx, K_HIST_LIMPEZAS, "[]")
        return try {
            val arr = org.json.JSONArray(bruto)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                o.getLong("t") to o.optInt("q", 0)
            }.sortedByDescending { it.first }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun registrarLimpezaRam(ctx: Context, qtdApps: Int) {
        val lista = historicoLimpezas(ctx).toMutableList()
        lista.add(0, System.currentTimeMillis() to qtdApps)
        val arr = org.json.JSONArray()
        lista.take(100).forEach { (t, q) ->
            arr.put(org.json.JSONObject().put("t", t).put("q", q))
        }
        Armazenamento.gravarTexto(ctx, K_HIST_LIMPEZAS, arr.toString())
    }

    // ---- Métricas de sessão Game Turbo ----
    private const val K_METRICA_SESSAO_INICIO = "gt_sessao_inicio"
    private const val K_METRICA_LIMPEZAS_SESSAO = "gt_sessao_limpezas"
    private const val K_METRICA_TOTAL_MIN = "gt_total_minutos"
    private const val K_METRICA_SESSOES = "gt_total_sessoes"

    fun iniciarSessaoGameTurbo(ctx: Context) {
        if (Armazenamento.lerLongo(ctx, K_METRICA_SESSAO_INICIO, 0L) == 0L) {
            Armazenamento.gravarLongo(ctx, K_METRICA_SESSAO_INICIO, System.currentTimeMillis())
            Armazenamento.gravarInteiro(ctx, K_METRICA_LIMPEZAS_SESSAO, 0)
            Armazenamento.gravarInteiro(ctx, K_METRICA_SESSOES, Armazenamento.lerInteiro(ctx, K_METRICA_SESSOES, 0) + 1)
        }
    }

    fun registrarLimpezaNaSessao(ctx: Context) {
        if (Armazenamento.lerLongo(ctx, K_METRICA_SESSAO_INICIO, 0L) > 0L) {
            Armazenamento.gravarInteiro(ctx, K_METRICA_LIMPEZAS_SESSAO, Armazenamento.lerInteiro(ctx, K_METRICA_LIMPEZAS_SESSAO, 0) + 1)
        }
    }

    fun finalizarSessaoGameTurbo(ctx: Context): Pair<Long, Int> {
        val inicio = Armazenamento.lerLongo(ctx, K_METRICA_SESSAO_INICIO, 0L)
        if (inicio == 0L) return 0L to 0
        val min = (System.currentTimeMillis() - inicio) / 60000L
        val limpezas = Armazenamento.lerInteiro(ctx, K_METRICA_LIMPEZAS_SESSAO, 0)
        Armazenamento.gravarLongo(ctx, K_METRICA_TOTAL_MIN, Armazenamento.lerLongo(ctx, K_METRICA_TOTAL_MIN, 0L) + min)
        Armazenamento.gravarLongo(ctx, K_METRICA_SESSAO_INICIO, 0L)
        Armazenamento.gravarInteiro(ctx, K_METRICA_LIMPEZAS_SESSAO, 0)
        return min to limpezas
    }

    fun totalMinutosGameTurbo(ctx: Context): Long = Armazenamento.lerLongo(ctx, K_METRICA_TOTAL_MIN, 0L)
    fun totalSessoesGameTurbo(ctx: Context): Int = Armazenamento.lerInteiro(ctx, K_METRICA_SESSOES, 0)

    // ---- Material You (cores dinâmicas) ----
    private const val K_MATERIAL_YOU = "material_you_ativo"
    fun materialYouAtivo(ctx: Context): Boolean = Armazenamento.lerBooleano(ctx, K_MATERIAL_YOU, false)
    fun definirMaterialYouAtivo(ctx: Context, ativo: Boolean) { Armazenamento.gravarBooleano(ctx, K_MATERIAL_YOU, ativo) }

    // ---- Não Perturbe (DND) agendado ----
    private const val K_DND_AGENDADO_ATIVO = "dnd_agendado_ativo"
    private const val K_DND_HORA_INICIO = "dnd_hora_inicio"
    private const val K_DND_MIN_INICIO = "dnd_min_inicio"
    private const val K_DND_HORA_FIM = "dnd_hora_fim"
    private const val K_DND_MIN_FIM = "dnd_min_fim"

    fun dndAgendadoAtivo(ctx: Context): Boolean = Armazenamento.lerBooleano(ctx, K_DND_AGENDADO_ATIVO, false)
    fun definirDndAgendadoAtivo(ctx: Context, ativo: Boolean) {
        Armazenamento.gravarBooleano(ctx, K_DND_AGENDADO_ATIVO, ativo)
        try { if (ativo) DndAgendador.agendar(ctx) else DndAgendador.cancelar(ctx) } catch (_: Exception) {}
    }
    fun dndHoraInicio(ctx: Context): Int = Armazenamento.lerInteiro(ctx, K_DND_HORA_INICIO, 22)
    fun definirDndHoraInicio(ctx: Context, h: Int) {
        Armazenamento.gravarInteiro(ctx, K_DND_HORA_INICIO, h.coerceIn(0, 23))
        if (dndAgendadoAtivo(ctx)) DndAgendador.agendar(ctx)
    }
    fun dndMinInicio(ctx: Context): Int = Armazenamento.lerInteiro(ctx, K_DND_MIN_INICIO, 0)
    fun definirDndMinInicio(ctx: Context, m: Int) {
        Armazenamento.gravarInteiro(ctx, K_DND_MIN_INICIO, m.coerceIn(0, 59))
        if (dndAgendadoAtivo(ctx)) DndAgendador.agendar(ctx)
    }
    fun dndHoraFim(ctx: Context): Int = Armazenamento.lerInteiro(ctx, K_DND_HORA_FIM, 7)
    fun definirDndHoraFim(ctx: Context, h: Int) {
        Armazenamento.gravarInteiro(ctx, K_DND_HORA_FIM, h.coerceIn(0, 23))
        if (dndAgendadoAtivo(ctx)) DndAgendador.agendar(ctx)
    }
    fun dndMinFim(ctx: Context): Int = Armazenamento.lerInteiro(ctx, K_DND_MIN_FIM, 0)
    fun definirDndMinFim(ctx: Context, m: Int) {
        Armazenamento.gravarInteiro(ctx, K_DND_MIN_FIM, m.coerceIn(0, 59))
        if (dndAgendadoAtivo(ctx)) DndAgendador.agendar(ctx)
    }

    // ---- Perfil de uso automático por bateria ----
    fun aplicarEconomiaSeBaixa(ctx: Context) {
        try {
            val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
            val nivel = bm.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
            if (nivel in 1..20) {
                definirPerfilUso(ctx, com.seguraturbo.app.model.PerfilUso.BATERIA)
            }
        } catch (_: Exception) {}
    }
}
