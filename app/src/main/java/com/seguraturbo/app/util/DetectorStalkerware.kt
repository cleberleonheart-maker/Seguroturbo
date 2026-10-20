package com.seguraturbo.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Detector de stalkerware/ratware conhecidos.
 *
 * O app não é um antivírus e não usa nuvem: a checagem é offline, comparando os
 * pacotes instalados com uma lista curada de apps de vigilância/espionagem
 * conhecidos publicamente (baseada em listas abertas como o Echap e pesquisas de
 * stalkerware). Nada é enviado para fora do aparelho.
 *
 * Para enxergar pacotes que se escondem da lista de apps, os pacotes conhecidos
 * são declarados no `<queries>` do AndroidManifest (exigência do Android 11+).
 */
object DetectorStalkerware {

    /** Um app de vigilância conhecido encontrado instalado. */
    data class Suspeito(
        val pacote: String,
        val nome: String,
        val categoria: String
    )

    private data class Assinatura(val pacote: String, val categoria: String)

    /**
     * Lista curada de pacotes conhecidos de stalkerware/ratware comercial.
     * Categorias: "rastreador", "monitor", "spyware".
     */
    private val CONHECIDOS: List<Assinatura> = listOf(
        Assinatura("com.mspy.mobile", "rastreador"),
        Assinatura("com.mspy.pro", "rastreador"),
        Assinatura("com.bsbsoft.mspy", "rastreador"),
        Assinatura("com.cyberlink.mobiletracker", "rastreador"),
        Assinatura("com.cocospy.android", "rastreador"),
        Assinatura("com.spyzie.android", "rastreador"),
        Assinatura("com.hoverwatch.mobile", "rastreador"),
        Assinatura("com.kidguard.monitor", "monitor"),
        Assinatura("com.iosnoops", "rastreador"),
        Assinatura("com.spyera.android", "rastreador"),
        Assinatura("com.minspy.monitor", "rastreador"),
        Assinatura("com.flexispy.android", "rastreador"),
        Assinatura("com.mobiletracker.android", "rastreador"),
        Assinatura("com.thetruthspy.guardian", "rastreador"),
        Assinatura("com.xnspy.android", "rastreador"),
        Assinatura("com.spyic.mobile", "rastreador"),
        Assinatura("com.highster.mobile", "rastreador"),
        Assinatura("com.mobiletracker.monitor", "rastreador"),
        Assinatura("com.wt.spy", "spyware"),
        Assinatura("com.familykeylogger", "spyware"),
        Assinatura("net.omniwave.spyphone", "spyware"),
        Assinatura("com.aim.spyphone", "spyware"),
        Assinatura("com.hiddenapp.hidespy", "spyware"),
        Assinatura("com.spyphone.android", "spyware")
    )

    private val SISTEMA_DE_BUSCA = CONHECIDOS.map { it.pacote }

    /**
     * Procura apps de vigilância conhecidos instalados. Retorna a lista (vazia se
     * não houver nada suspeito). Roda fora da main thread.
     */
    suspend fun procurar(ctx: Context): List<Suspeito> = withContext(Dispatchers.IO) {
        val pm = ctx.packageManager
        CONHECIDOS.mapNotNull { assinatura ->
            if (instalado(pm, assinatura.pacote)) {
                Suspeito(
                    pacote = assinatura.pacote,
                    nome = rotulo(pm, assinatura.pacote),
                    categoria = assinatura.categoria
                )
            } else {
                null
            }
        }
    }

    /** Pacotes conhecidos declarados no `<queries>` (usado nos testes/manutenção). */
    fun pacotesConhecidos(): List<String> = SISTEMA_DE_BUSCA

    private fun instalado(pm: PackageManager, pacote: String): Boolean = try {
        pm.getPackageInfo(pacote, 0)
        true
    } catch (e: Exception) {
        false
    }

    private fun rotulo(pm: PackageManager, pacote: String): String = try {
        val info = pm.getPackageInfo(pacote, 0).applicationInfo
        runCatching { info.loadLabel(pm).toString() }.getOrDefault(pacote)
    } catch (e: Exception) {
        pacote
    }

    /**
     * Heurística leve para apps com atividade de launcher que combinam sinais
     * típicos de vigilância: acessibilidade + leitura de SMS/contatos + localização
     * concedidos ao mesmo tempo. Não é prova (apps legítimos podem pedir isso),
     * por isso o risco é baixo e a descrição é explicativa.
     */
    suspend fun combinacaoSuspeita(ctx: Context): List<String> = withContext(Dispatchers.IO) {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolvidos = try {
            pm.queryIntentActivities(intent, 0)
        } catch (e: Exception) {
            return@withContext emptyList()
        }
        resolvidos.asSequence()
            .mapNotNull { it.activityInfo?.applicationInfo }
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .distinctBy { it.packageName }
            .filter { it.packageName != ctx.packageName }
            .filter { app -> combinacaoPerigosa(pm, app.packageName) }
            .map { runCatching { it.loadLabel(pm).toString() }.getOrDefault(it.packageName) }
            .sorted()
            .toList()
    }

    private fun combinacaoPerigosa(pm: PackageManager, pacote: String): Boolean = try {
        val info = pm.getPackageInfo(pacote, PackageManager.GET_PERMISSIONS)
        combinacaoPerigosaDe(info.requestedPermissions?.toSet() ?: emptySet())
    } catch (e: Exception) {
        false
    }

    /**
     * Regra pura: um app é "combinação suspeita" quando pede ao mesmo tempo ao
     * menos três dos quatro sinais típicos de vigilância (SMS, localização,
     * contatos, microfone). Função sem Android para facilitar os testes.
     */
    fun combinacaoPerigosaDe(permissoes: Set<String>): Boolean {
        val sinais = listOf(
            "android.permission.READ_SMS" in permissoes ||
                "android.permission.RECEIVE_SMS" in permissoes,
            "android.permission.ACCESS_FINE_LOCATION" in permissoes,
            "android.permission.READ_CONTACTS" in permissoes,
            "android.permission.RECORD_AUDIO" in permissoes
        )
        return sinais.count { it } >= 3
    }
}
