package com.seguraturbo.app.util

import android.content.Context
import com.seguraturbo.app.model.HistoricoEntrada

/**
 * Aplica as ações do Modo Jogo fora da UI (Turbo agendado).
 * Mantém a mesma lógica de efeitos colaterais usada pela GameTurboScreen,
 * sem depender de ViewModel.
 */
object TurboAgendadoAplicador {

    fun ligar(ctx: Context) {
        if (Preferencias.dndAuto(ctx)) {
            if (!ModoConcentracao.ativar(ctx)) {
                // Sem acesso: ignora silenciosamente (não há UI para pedir).
            }
        }
        if (Preferencias.silenciarMidia(ctx)) {
            SilenciarMidia.silenciar(ctx)
        }
        if (Preferencias.autoLimparRamJogo(ctx)) {
            val apps = GerenciadorRam.appsFechaveis(ctx)
            if (apps.isNotEmpty()) {
                GerenciadorRam.liberar(ctx, apps.map { it.pacote })
            }
        }
        registrarHistorico(ctx, ativado = true)
    }

    fun desligar(ctx: Context) {
        if (Preferencias.dndAuto(ctx)) {
            ModoConcentracao.restaurar(ctx)
        }
        if (Preferencias.silenciarMidia(ctx)) {
            SilenciarMidia.restaurar(ctx)
        }
        registrarHistorico(ctx, ativado = false)
    }

    private fun registrarHistorico(ctx: Context, ativado: Boolean) {
        HistoricoStore.adicionar(
            ctx,
            HistoricoEntrada(
                tipo = HistoricoEntrada.Tipo.TURBO,
                data = System.currentTimeMillis(),
                titulo = if (ativado) "Modo Jogo ativado (agendado)" else "Modo Jogo desativado (agendado)",
                detalhe = "Turbo agendado"
            )
        )
    }
}
