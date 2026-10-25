package com.seguraturbo.app.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo

/**
 * Limpeza de RAM. No Android moderno um app comum não consegue listar os
 * processos em execução de outros apps, então oferecemos a lista de apps
 * instalados (visíveis, não de sistema) e encerramos os processos em segundo
 * plano dos escolhidos via [ActivityManager.killBackgroundProcesses].
 *
 * Limitações reais (explicadas ao usuário): apps com serviço em primeiro plano
 * ou reabertos pelo sistema podem voltar; o ganho é temporário.
 */
object GerenciadorRam {

    /** Memória do aparelho, em megabytes. */
    data class Memoria(val totalMb: Long, val livreMb: Long) {
        val usadaMb: Long get() = (totalMb - livreMb).coerceAtLeast(0)

        /** Percentual de memória em uso (0–100). */
        val percentualUsado: Int
            get() = if (totalMb <= 0) 0 else ((usadaMb * 100) / totalMb).toInt()
    }

    /** Um app que pode ser encerrado. */
    data class AppAlvo(val pacote: String, val nome: String)

    /** Lê a memória total e livre atuais. */
    fun memoria(ctx: Context): Memoria {
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return Memoria(
            totalMb = info.totalMem / (1024L * 1024L),
            livreMb = info.availMem / (1024L * 1024L)
        )
    }

    /**
     * Apps que podem ser encerrados: com atividade de launcher, não de sistema e
     * diferentes do próprio SeguroTurbo. Ordenados pelo nome.
     */
    fun appsFechaveis(ctx: Context): List<AppAlvo> {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolvidos = try {
            pm.queryIntentActivities(intent, 0)
        } catch (e: Exception) {
            return emptyList()
        }
        return resolvidos.asSequence()
            .mapNotNull { it.activityInfo?.applicationInfo }
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .filter { it.packageName != ctx.packageName }
            .distinctBy { it.packageName }
            .map {
                AppAlvo(
                    pacote = it.packageName,
                    nome = runCatching { it.loadLabel(pm).toString() }.getOrDefault(it.packageName)
                )
            }
            .sortedBy { it.nome.lowercase() }
            .toList()
    }

    /**
     * Encerra os processos em segundo plano dos pacotes escolhidos.
     * Devolve quantos pacotes foram solicitados com sucesso.
     */
    fun liberar(ctx: Context, pacotes: List<String>): Int {
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var encerrados = 0
        for (pacote in pacotes.distinct()) {
            try {
                am.killBackgroundProcesses(pacote)
                encerrados++
            } catch (e: Exception) {
                // Pacote protegido ou sem permissão: ignora e segue.
            }
        }
        try {
            if (encerrados > 0) {
                Preferencias.registrarLimpezaRam(ctx, encerrados)
                Preferencias.registrarLimpezaNaSessao(ctx)
            }
        } catch (_: Exception) {
        }
        return encerrados
    }
}
