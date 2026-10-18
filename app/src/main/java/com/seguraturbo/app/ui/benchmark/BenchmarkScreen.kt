package com.seguraturbo.app.ui.benchmark

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seguraturbo.app.R
import com.seguraturbo.app.model.BenchmarkResult
import com.seguraturbo.app.ui.components.BarraProgressoNeon
import com.seguraturbo.app.ui.components.BotaoNeon
import com.seguraturbo.app.ui.components.CartaoFuturista
import com.seguraturbo.app.ui.components.GraficoLinha
import com.seguraturbo.app.ui.components.HistoricoSecao
import com.seguraturbo.app.ui.components.LinhaInfo
import com.seguraturbo.app.ui.components.TituloSecao
import com.seguraturbo.app.ui.theme.Cores
import com.seguraturbo.app.util.DeviceInfo
import com.seguraturbo.app.util.Comparacao
import com.seguraturbo.app.util.RelatorioImagem
import com.seguraturbo.app.util.RelatorioPdf
import java.util.Locale

@Composable
fun BenchmarkScreen(vm: BenchmarkViewModel = viewModel()) {
    val contexto = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { CartaoDispositivo(vm.info) }

        item {
            BotaoNeon(
                texto = stringResource(if (vm.testando) R.string.bm_testing else R.string.bm_start),
                onClick = { vm.iniciar() },
                habilitado = !vm.testando,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (vm.testando) {
            item {
                CartaoFuturista {
                    Text(
                        stringResource(R.string.bm_testing_progress, vm.progresso),
                        color = Cores.Ciano,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    BarraProgressoNeon(progresso = vm.progresso / 100f)
                }
            }
        }

        vm.resultado?.let { r ->
            item {
                CartaoFuturista {
                    Text(
                        stringResource(R.string.bm_total_score),
                        color = Cores.TextoCinza,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    val animado by animateIntAsState(
                        targetValue = r.total,
                        animationSpec = tween(1200),
                        label = "pontuacao"
                    )
                    Text(
                        String.format(Locale.getDefault(), "%,d", animado),
                        color = Cores.Ciano,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            item {
                CartaoFuturista {
                    TituloSecao(stringResource(R.string.bm_results))
                    Spacer(Modifier.height(14.dp))
                    MetricaLinha(stringResource(R.string.bm_cpu), r.cpu, 250_000f, Cores.Ciano)
                    Spacer(Modifier.height(12.dp))
                    MetricaLinha(stringResource(R.string.bm_gpu), r.gpu, 300_000f, Cores.Roxo)
                    Spacer(Modifier.height(12.dp))
                    MetricaLinha(stringResource(R.string.bm_mem), r.mem, 220_000f, Cores.Verde)
                    Spacer(Modifier.height(12.dp))
                    MetricaLinha(stringResource(R.string.bm_ux), r.ux, 160_000f, Cores.Amarelo)
                }
            }
            item {
                CartaoFuturista {
                    TituloSecao(stringResource(R.string.bm_hw_title))
                    Spacer(Modifier.height(10.dp))
                    if (r.gpuFps > 0) {
                        LinhaInfo(
                            stringResource(R.string.bm_gpu_fps),
                            stringResource(R.string.bm_gpu_fps_value, r.gpuFps)
                        )
                    }
                    if (r.armazenamentoMbs > 0.0) {
                        LinhaInfo(
                            stringResource(R.string.bm_storage_speed),
                            stringResource(R.string.bm_storage_speed_value, r.armazenamentoMbs)
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BotaoNeon(
                        texto = stringResource(R.string.share_image),
                        onClick = { compartilhar(contexto, r, vm.info, pdf = false) },
                        cor = Cores.Verde,
                        modifier = Modifier.weight(1f).height(48.dp)
                    )
                    BotaoNeon(
                        texto = stringResource(R.string.share_pdf),
                        onClick = { compartilhar(contexto, r, vm.info, pdf = true) },
                        cor = Cores.Ciano,
                        modifier = Modifier.weight(1f).height(48.dp)
                    )
                }
            }
        }

        item { CartaoRede(vm) }

        if (vm.pontuacoes.isNotEmpty()) {
            val comparacao = Comparacao.resumo(vm.pontuacoes, vm.resultado?.total)
            item {
                CartaoFuturista {
                    TituloSecao(stringResource(R.string.bm_compare_title))
                    Spacer(Modifier.height(10.dp))
                    comparacao.media?.let {
                        LinhaInfo(
                            stringResource(R.string.bm_compare_avg),
                            stringResource(R.string.bm_compare_points, it)
                        )
                    }
                    comparacao.melhor?.let {
                        LinhaInfo(
                            stringResource(R.string.bm_compare_best),
                            stringResource(R.string.bm_compare_points, it)
                        )
                    }
                    comparacao.posicao?.let {
                        LinhaInfo(
                            stringResource(R.string.bm_compare_rank),
                            stringResource(
                                R.string.bm_compare_rank_value,
                                it,
                                comparacao.total
                            )
                        )
                    }
                }
            }
        }

        if (vm.pontuacoes.size >= 2) {
            item {
                CartaoFuturista {
                    TituloSecao(stringResource(R.string.bm_history_chart))
                    Spacer(Modifier.height(12.dp))
                    GraficoLinha(valores = vm.pontuacoes, cor = Cores.Ciano)
                    Spacer(Modifier.height(14.dp))
                    BotaoNeon(
                        texto = stringResource(R.string.bm_share_chart),
                        onClick = { compartilharGrafico(contexto, vm.pontuacoes) },
                        cor = Cores.Ciano,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            CartaoFuturista {
                HistoricoSecao(vm.historico, onLimpar = { vm.limparHistorico() })
            }
        }
    }
}

@Composable
private fun CartaoRede(vm: BenchmarkViewModel) {
    val resultado = vm.rede
    CartaoFuturista {
        TituloSecao(stringResource(R.string.rede_titulo))
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.rede_desc),
            color = Cores.TextoCinza,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(12.dp))
        BotaoNeon(
            texto = stringResource(
                if (vm.testandoRede) R.string.rede_btn_testando else R.string.rede_btn
            ),
            onClick = { vm.testarRede() },
            habilitado = !vm.testandoRede,
            cor = Cores.Roxo,
            modifier = Modifier.fillMaxWidth().height(44.dp)
        )
        if (!vm.testandoRede) {
            resultado?.let { r ->
                Spacer(Modifier.height(12.dp))
                r.latenciaMs?.let {
                    LinhaInfo(
                        stringResource(R.string.rede_latencia),
                        stringResource(R.string.rede_latencia_valor, it)
                    )
                }
                r.downloadMbps?.let {
                    LinhaInfo(
                        stringResource(R.string.rede_download),
                        stringResource(R.string.rede_download_valor, it)
                    )
                }
                if (!r.temDados) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.rede_falhou),
                        color = Cores.Amarelo,
                        fontSize = 12.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.rede_aviso),
            color = Cores.TextoCinza,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun CartaoDispositivo(info: DeviceInfo.Info?) {    CartaoFuturista {
        TituloSecao(stringResource(R.string.bm_device_info))
        Spacer(Modifier.height(10.dp))
        if (info == null) {
            Text(stringResource(R.string.bm_loading), color = Cores.TextoCinza, fontSize = 13.sp)
        } else {
            LinhaInfo(stringResource(R.string.bm_model), "${info.fabricante} ${info.modelo}")
            LinhaInfo(stringResource(R.string.bm_chipset), info.chipset)
            LinhaInfo(stringResource(R.string.bm_cores), info.nucleos.toString())
            LinhaInfo(
                stringResource(R.string.bm_ram),
                stringResource(R.string.bm_ram_value, info.ramTotalMb, info.ramLivreMb)
            )
            LinhaInfo(
                stringResource(R.string.bm_storage),
                stringResource(
                    R.string.bm_storage_value,
                    info.armazenamentoLivreGb,
                    info.armazenamentoTotalGb
                )
            )
            LinhaInfo(stringResource(R.string.bm_system), info.versaoAndroid)
        }
    }
}

/** Monta o relatório (imagem ou PDF) do resultado e abre o compartilhamento. */
private fun compartilhar(ctx: Context, r: BenchmarkResult, info: DeviceInfo.Info?, pdf: Boolean) {
    val linhas = buildList {
        info?.let { add(ctx.getString(R.string.rep_aparelho) to "${it.fabricante} ${it.modelo}") }
        info?.let { add(ctx.getString(R.string.rep_chipset) to it.chipset) }
        add(ctx.getString(R.string.rep_total) to "${r.total}")
        add("CPU" to "${r.cpu}")
        add("GPU" to "${r.gpu}")
        add("MEM (RAM)" to "${r.mem}")
        add("UX" to "${r.ux}")
        if (r.gpuFps > 0) add("GPU (OpenGL)" to "${r.gpuFps} FPS")
        if (r.armazenamentoMbs > 0.0) {
            add(
                ctx.getString(R.string.rep_armazenamento)
                    to String.format(Locale.getDefault(), "%.0f MB/s", r.armazenamentoMbs)
            )
        }
    }
    val titulo = ctx.getString(R.string.rep_bm_title)
    val subtitulo = ctx.getString(R.string.rep_bm_subtitle)
    val rotulo = ctx.getString(R.string.bm_share)
    if (pdf) {
        val uri = RelatorioPdf.gerar(ctx, titulo, subtitulo, null, linhas) ?: return
        RelatorioPdf.compartilhar(ctx, uri, rotulo)
    } else {
        val uri = RelatorioImagem.gerar(ctx, titulo, subtitulo, null, linhas) ?: return
        RelatorioImagem.compartilhar(ctx, uri, rotulo)
    }
}

/** Gera a imagem do gráfico de evolução das pontuações e abre o compartilhamento. */
private fun compartilharGrafico(ctx: Context, pontuacoes: List<Int>) {
    val uri = RelatorioImagem.gerarGrafico(
        ctx,
        ctx.getString(R.string.rep_bm_title),
        ctx.getString(R.string.bm_history_chart),
        pontuacoes
    ) ?: return
    RelatorioImagem.compartilhar(ctx, uri, ctx.getString(R.string.bm_share_chart))
}

@Composable
private fun MetricaLinha(rotulo: String, valor: Int, maximo: Float, cor: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(rotulo, color = Cores.TextoCinza, fontSize = 13.sp)
            Text(
                String.format(Locale.getDefault(), "%,d", valor),
                color = cor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
        }
        Spacer(Modifier.height(6.dp))
        BarraProgressoNeon(progresso = valor / maximo, cor = cor)
    }
}
