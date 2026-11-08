package com.seguraturbo.app.util

import android.content.Context
import com.seguraturbo.app.model.EstadoPerfis
import com.seguraturbo.app.model.PerfilDesempenho
import com.seguraturbo.app.model.PerfilUso

/**
 * Aplica um perfil de uso: um preset que ajusta várias preferências de uma vez
 * (perfil de desempenho, Não Perturbe, trava de tela, silenciar mídia, lembrete e
 * atualização automática). Tudo é gravado localmente, sem conta nem nuvem.
 *
 * O perfil não é um "modo travado": depois de aplicar, o usuário pode ajustar cada
 * item individualmente. Ele só serve como ponto de partida.
 */
object PerfisUso {

    /** Configuração que cada perfil aplica. */
    data class Config(
        val perfilDesempenho: PerfilDesempenho,
        val dndAuto: Boolean,
        val travaTela: Boolean,
        val silenciarMidia: Boolean,
        val lembrete: Boolean,
        val atualizacaoAuto: Boolean
    )

    fun config(perfil: PerfilUso): Config = when (perfil) {
        PerfilUso.PADRAO -> Config(
            perfilDesempenho = PerfilDesempenho.EQUILIBRADO,
            dndAuto = false,
            travaTela = false,
            silenciarMidia = false,
            lembrete = false,
            atualizacaoAuto = true
        )
        PerfilUso.GAMER -> Config(
            perfilDesempenho = PerfilDesempenho.TURBO,
            dndAuto = true,
            travaTela = true,
            silenciarMidia = true,
            lembrete = false,
            atualizacaoAuto = true
        )
        PerfilUso.CASUAL -> Config(
            perfilDesempenho = PerfilDesempenho.EQUILIBRADO,
            dndAuto = false,
            travaTela = false,
            silenciarMidia = false,
            lembrete = true,
            atualizacaoAuto = true
        )
        PerfilUso.BATERIA -> Config(
            perfilDesempenho = PerfilDesempenho.ECONOMIA,
            dndAuto = false,
            travaTela = false,
            silenciarMidia = false,
            lembrete = false,
            atualizacaoAuto = false
        )
        PerfilUso.SEGURANCA_MAXIMA -> Config(
            perfilDesempenho = PerfilDesempenho.EQUILIBRADO,
            dndAuto = false,
            travaTela = false,
            silenciarMidia = false,
            lembrete = true,
            atualizacaoAuto = true
        )
    }

    /**
     * Grava as preferências do perfil. Retorna true se ativou o lembrete
     * (para o chamador pedir a permissão de notificação, se necessário).
     */
    fun aplicar(ctx: Context, perfil: PerfilUso): Boolean {
        val c = config(perfil)
        Preferencias.definirPerfilDesempenho(ctx, c.perfilDesempenho)
        Preferencias.definirDndAuto(ctx, c.dndAuto)
        Preferencias.definirTravaTelaJogo(ctx, c.travaTela)
        Preferencias.definirSilenciarMidia(ctx, c.silenciarMidia)
        Preferencias.definirLembreteAtivo(ctx, c.lembrete)
        Preferencias.definirAtualizacaoAutomatica(ctx, c.atualizacaoAuto)
        Preferencias.definirPerfilUso(ctx, perfil)

        // Reagenda os serviços que dependem dessas chaves.
        if (c.lembrete) Agendador.agendar(ctx) else Agendador.cancelar(ctx)
        if (c.atualizacaoAuto) AgendadorAtualizacao.agendar(ctx)
        else AgendadorAtualizacao.cancelar(ctx)

        return c.lembrete
    }

    /**
     * Fotografia de todos os ajustes que um perfil de uso mexe. É o que o modo
     * economia automático guarda antes de assumir o perfil Bateria, para
     * devolver tudo exatamente como estava (inclusive ajustes que o usuário fez
     * depois de escolher o perfil).
     */
    fun estadoAtual(ctx: Context): EstadoPerfis = EstadoPerfis(
        perfilUso = Preferencias.perfilUso(ctx),
        perfilDesempenho = Preferencias.perfilDesempenho(ctx),
        dndAuto = Preferencias.dndAuto(ctx),
        travaTela = Preferencias.travaTelaJogo(ctx),
        silenciarMidia = Preferencias.silenciarMidia(ctx),
        lembrete = Preferencias.lembreteAtivo(ctx),
        atualizacaoAuto = Preferencias.atualizacaoAutomatica(ctx)
    )

    /**
     * Devolve o estado guardado. Não reaplica um preset: cada ajuste volta ao
     * valor que tinha antes, e os agendadores são reprogramados.
     */
    fun restaurar(ctx: Context, estado: EstadoPerfis) {
        Preferencias.definirPerfilDesempenho(ctx, estado.perfilDesempenho)
        Preferencias.definirDndAuto(ctx, estado.dndAuto)
        Preferencias.definirTravaTelaJogo(ctx, estado.travaTela)
        Preferencias.definirSilenciarMidia(ctx, estado.silenciarMidia)
        Preferencias.definirLembreteAtivo(ctx, estado.lembrete)
        Preferencias.definirAtualizacaoAutomatica(ctx, estado.atualizacaoAuto)
        Preferencias.definirPerfilUso(ctx, estado.perfilUso)

        if (estado.lembrete) Agendador.agendar(ctx) else Agendador.cancelar(ctx)
        if (estado.atualizacaoAuto) AgendadorAtualizacao.agendar(ctx)
        else AgendadorAtualizacao.cancelar(ctx)
    }
}
