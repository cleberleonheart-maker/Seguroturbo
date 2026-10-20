package com.seguraturbo.app.util

import android.content.Context
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
}
