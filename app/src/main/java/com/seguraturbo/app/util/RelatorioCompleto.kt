package com.seguraturbo.app.util

import android.content.Context
import com.seguraturbo.app.model.HistoricoEntrada
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Monta um relatório completo em ZIP, tudo offline: backup das preferências,
 * inventário de permissões, histórico de ações, histórico de limpezas de RAM,
 * métricas do Game Turbo e informações do dispositivo.
 */
object RelatorioCompleto {

    private val formatoNome = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
    private val formatoData = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    suspend fun gerar(ctx: Context): ByteArray = withContext(Dispatchers.IO) {
        val apps = AuditoriaPermissoes.listar(ctx)
        val entradas = ArrayList<Pair<String, String>>()

        entradas += "backup.json" to Backup.montar(ctx)

        entradas += "inventario-permissoes.csv" to InventarioCsv.montarCsv(apps) {
            ctx.getString(it.rotuloRes)
        }

        entradas += "historico-acoes.csv" to historicoCsv(HistoricoStore.listar(ctx))

        val limpezas = Preferencias.historicoLimpezas(ctx)
        entradas += "historico-limpezas-ram.csv" to limpezasCsv(limpezas)

        entradas += "metricas-gameturbo.csv" to metricasGameTurboCsv(ctx, limpezas)

        entradas += "dispositivo.txt" to dispositivoTxt(ctx)

        val saida = ByteArrayOutputStream()
        ZipOutputStream(saida).use { zip ->
            entradas.forEach { (nome, conteudo) ->
                zip.putNextEntry(ZipEntry(nome))
                zip.write(conteudo.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
        saida.toByteArray()
    }

    fun nomeSugerido(): String =
        "seguroturbo-relatorio-${formatoNome.format(Date())}.zip"

    private fun historicoCsv(lista: List<HistoricoEntrada>): String {
        val sb = StringBuilder()
        sb.append("data;tipo;titulo;detalhe\n")
        lista.sortedByDescending { it.data }.forEach { e ->
            sb.append(campo(formatoData.format(Date(e.data)))).append(';')
                .append(campo(e.tipo.name)).append(';')
                .append(campo(e.titulo)).append(';')
                .append(campo(e.detalhe)).append('\n')
        }
        return sb.toString()
    }

    private fun limpezasCsv(lista: List<Pair<Long, Int>>): String {
        val sb = StringBuilder()
        sb.append("data;apps_encerrados\n")
        lista.forEach { (t, q) ->
            sb.append(campo(formatoData.format(Date(t)))).append(';')
                .append(q).append('\n')
        }
        return sb.toString()
    }

    private fun metricasGameTurboCsv(ctx: Context, limpezas: List<Pair<Long, Int>>): String {
        val sb = StringBuilder()
        sb.append("metrica;valor\n")
        sb.append("tempo_total_minutos;").append(Preferencias.totalMinutosGameTurbo(ctx)).append('\n')
        sb.append("total_sessoes;").append(Preferencias.totalSessoesGameTurbo(ctx)).append('\n')
        sb.append("total_limpezas_ram;").append(limpezas.sumOf { it.second }).append('\n')
        return sb.toString()
    }

    private fun dispositivoTxt(ctx: Context): String {
        val i = DeviceInfo.coletar(ctx)
        return buildString {
            appendLine("SeguroTurbo - Relatório do dispositivo")
            appendLine("Gerado em: ${formatoData.format(Date())}")
            appendLine()
            appendLine("Fabricante: ${i.fabricante}")
            appendLine("Modelo: ${i.modelo}")
            appendLine("Chipset: ${i.chipset}")
            appendLine("Núcleos: ${i.nucleos}")
            appendLine("RAM total: ${i.ramTotalMb} MB")
            appendLine("RAM livre: ${i.ramLivreMb} MB")
            appendLine("Armazenamento total: ${"%.1f".format(Locale.US, i.armazenamentoTotalGb)} GB")
            appendLine("Armazenamento livre: ${"%.1f".format(Locale.US, i.armazenamentoLivreGb)} GB")
            appendLine("Versão: ${i.versaoAndroid}")
        }
    }

    private fun campo(valor: String): String = "\"" + valor.replace("\"", "\"\"") + "\""
}
