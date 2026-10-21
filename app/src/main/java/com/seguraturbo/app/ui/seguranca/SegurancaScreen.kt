package com.seguraturbo.app.ui.seguranca

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seguraturbo.app.R
import com.seguraturbo.app.model.AcaoConfig
import com.seguraturbo.app.model.Threat
import com.seguraturbo.app.ui.components.BarraProgressoNeon
import com.seguraturbo.app.ui.components.BotaoNeon
import com.seguraturbo.app.ui.components.CartaoFuturista
import com.seguraturbo.app.ui.components.DetalheAmeacaDialog
import com.seguraturbo.app.ui.components.HistoricoSecao
import com.seguraturbo.app.ui.components.PermissoesDialog
import com.seguraturbo.app.ui.components.SeloStatus
import com.seguraturbo.app.ui.components.TituloSecao
import com.seguraturbo.app.ui.theme.Cores
import com.seguraturbo.app.util.AcoesSistema
import com.seguraturbo.app.util.AtalhoAcao
import com.seguraturbo.app.util.AtalhoPendente
import com.seguraturbo.app.util.AuditoriaPermissoes
import com.seguraturbo.app.util.RiscoApp
import com.seguraturbo.app.util.RelatorioImagem
import com.seguraturbo.app.util.RelatorioPdf

@Composable
fun SegurancaScreen(vm: SegurancaViewModel = viewModel()) {
    val contexto = LocalContext.current
    var selecionada by remember { mutableStateOf<Threat?>(null) }
    var mostrarPermissoes by remember { mutableStateOf(false) }

    // Atalho "Escanear" do ícone: dispara a varredura ao abrir a tela.
    LaunchedEffect(Unit) {
        if (AtalhoPendente.consumir() == AtalhoAcao.ESCANEAR) vm.escanear()
    }
    val transicao = rememberInfiniteTransition(label = "pulso")
    val pulso by transicao.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "pulso"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { CartaoStatus(vm, pulso) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotaoNeon(
                    texto = stringResource(R.string.sec_scan_button),
                    onClick = { vm.escanear() },
                    habilitado = !vm.escaneando && !vm.limpando,
                    modifier = Modifier.weight(1f)
                )
                BotaoNeon(
                    texto = stringResource(R.string.sec_clean_button),
                    onClick = { vm.limpar() },
                    habilitado = !vm.limpando && !vm.escaneando,
                    cor = Cores.Verde,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { CartaoAuditoriaPermissoes(vm, { mostrarPermissoes = true }) }

        if (vm.limpando || vm.mensagemLimpeza != null) {
            item {
                CartaoFuturista {
                    TituloSecao(stringResource(R.string.sec_clean_title))
                    Spacer(Modifier.height(12.dp))
                    if (vm.limpando) {
                        Text(
                            stringResource(R.string.sec_clean_progress, vm.progressoLimpeza),
                            color = Cores.Ciano,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        BarraProgressoNeon(progresso = vm.progressoLimpeza / 100f, cor = Cores.Verde)
                    } else {
                        Text(
                            vm.mensagemLimpeza ?: "",
                            color = Cores.Verde,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        if (vm.jaEscaneou && !vm.escaneando) {
            vm.score?.let { score ->
                item { CartaoScore(score) }
            }
        }

        if (vm.jaEscaneou && !vm.escaneando) {
            if (vm.ameacas.isEmpty()) {
                item {
                    CartaoFuturista {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✅", fontSize = 22.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                stringResource(R.string.sec_no_threats),
                                color = Cores.Verde,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                item { TituloSecao(stringResource(R.string.sec_alerts_title)) }
                if (vm.categorias.size > 1) {
                    item { ChipsFiltroCategoria(vm) }
                }
                items(vm.ameacasFiltradas) { ameaca ->
                    CartaoAmeaca(
                        ameaca = ameaca,
                        onAbrir = { selecionada = ameaca },
                        onCorrigir = {
                            if (ameaca.acao == AcaoConfig.CACHE) {
                                vm.limpar()
                            } else {
                                AcoesSistema.abrirConfiguracao(contexto, ameaca.acao, ameaca.pacoteApp)
                            }
                        }
                    )
                }
            }
        }

        if (vm.jaEscaneou && !vm.escaneando) {
            vm.score?.let { score ->
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BotaoNeon(
                            texto = stringResource(R.string.share_image),
                            onClick = { compartilharScan(contexto, vm, score, pdf = false) },
                            cor = Cores.Verde,
                            modifier = Modifier.weight(1f).height(48.dp)
                        )
                        BotaoNeon(
                            texto = stringResource(R.string.share_pdf),
                            onClick = { compartilharScan(contexto, vm, score, pdf = true) },
                            cor = Cores.Ciano,
                            modifier = Modifier.weight(1f).height(48.dp)
                        )
                    }
                }
            }
        }

        item {
            CartaoFuturista {
                HistoricoSecao(vm.historico, onLimpar = { vm.limparHistorico() })
            }
        }
    }

    selecionada?.let { ameaca ->
        DetalheAmeacaDialog(
            ameaca = ameaca,
            onCorrigir = {
                selecionada = null
                if (ameaca.acao == AcaoConfig.CACHE) {
                    vm.limpar()
                } else {
                    AcoesSistema.abrirConfiguracao(contexto, ameaca.acao, ameaca.pacoteApp)
                }
            },
            onFechar = { selecionada = null }
        )
    }

    if (mostrarPermissoes) {
        PermissoesDialog(onFechar = { mostrarPermissoes = false })
    }
}

/** Gera o relatório (imagem ou PDF) do resultado do scan e abre o compartilhamento. */
private fun compartilharScan(ctx: Context, vm: SegurancaViewModel, score: Int, pdf: Boolean) {
    val linhas = buildList {
        add(ctx.getString(R.string.rep_alertas) to vm.ameacas.size.toString())
        vm.ameacas.take(8).forEach { add(it.nome to it.risco) }
        if (vm.ameacas.isEmpty()) add(ctx.getString(R.string.rep_situacao) to ctx.getString(R.string.rep_tudo_certo))
    }
    val titulo = ctx.getString(R.string.rep_sec_title)
    val subtitulo = ctx.getString(R.string.rep_sec_subtitle)
    val rotulo = ctx.getString(R.string.sec_share)
    if (pdf) {
        val uri = RelatorioPdf.gerar(ctx, titulo, subtitulo, score, linhas) ?: return
        RelatorioPdf.compartilhar(ctx, uri, rotulo)
    } else {
        val uri = RelatorioImagem.gerar(ctx, titulo, subtitulo, score, linhas) ?: return
        RelatorioImagem.compartilhar(ctx, uri, rotulo)
    }
}

@Composable
private fun CartaoScore(score: Int) {
    val cor = Cores.corPorScore(score)
    val animado by animateIntAsState(targetValue = score, animationSpec = tween(900), label = "score")
    CartaoFuturista {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.sec_score_title),
                    color = Cores.TextoCinza,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(R.string.sec_score_value, animado),
                    color = cor,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                when {
                    score >= 80 -> "🛡️"
                    score >= 50 -> "⚠️"
                    else -> "🚨"
                },
                fontSize = 34.sp
            )
        }
        Spacer(Modifier.height(10.dp))
        BarraProgressoNeon(progresso = animado / 100f, cor = cor)
    }
}

@Composable
private fun CartaoStatus(vm: SegurancaViewModel, pulso: Float) {
    val cor = when {
        vm.escaneando -> Cores.Ciano
        !vm.jaEscaneou -> Cores.TextoCinza
        vm.ameacas.isEmpty() -> Cores.Verde
        else -> Cores.Vermelho
    }
    val titulo = when {
        vm.escaneando -> stringResource(R.string.sec_status_scanning)
        !vm.jaEscaneou -> stringResource(R.string.sec_status_ready)
        vm.ameacas.isEmpty() -> stringResource(R.string.sec_status_secure)
        else -> stringResource(R.string.sec_status_alerts, vm.ameacas.size)
    }

    CartaoFuturista {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(50))
                    .background(cor.copy(alpha = if (vm.escaneando) 0.15f * pulso else 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🛡️", fontSize = 30.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(titulo, color = cor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.sec_subtitle),
                    color = Cores.TextoCinza,
                    fontSize = 12.sp
                )
            }
        }
        if (vm.escaneando) {
            Spacer(Modifier.height(14.dp))
            BarraProgressoNeon(progresso = pulso)
        }
    }
}

@Composable
private fun CartaoAuditoriaPermissoes(vm: SegurancaViewModel, onAbrir: () -> Unit) {
    val alto = vm.appsEmRisco.count { AuditoriaPermissoes.riscoDe(it) == RiscoApp.ALTO }
    val medio = vm.appsEmRisco.count { AuditoriaPermissoes.riscoDe(it) == RiscoApp.MEDIO }
    CartaoFuturista {
        TituloSecao(stringResource(R.string.perm_titulo))
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.perm_desc),
            color = Cores.TextoCinza,
            fontSize = 12.sp
        )
        if (vm.appsEmRisco.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(
                    R.string.perm_risco_perfis,
                    alto,
                    medio,
                    vm.appsEmRisco.size - alto - medio
                ),
                color = Cores.Ciano,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(12.dp))
        BotaoNeon(
            texto = stringResource(R.string.perm_abrir),
            onClick = onAbrir,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ChipsFiltroCategoria(vm: SegurancaViewModel) {
    val selecionada = vm.categoriaFiltro
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 2.dp)
    ) {
        item {
            ChipFiltro(
                texto = stringResource(R.string.sec_filtro_todas),
                contagem = vm.ameacas.size,
                ativo = selecionada == null,
                onClick = { vm.categoriaFiltro = null }
            )
        }
        items(vm.categorias) { categoria ->
            ChipFiltro(
                texto = rotuloCategoria(categoria),
                contagem = vm.ameacas.count { it.tipo == categoria },
                ativo = selecionada == categoria,
                onClick = {
                    vm.categoriaFiltro = if (selecionada == categoria) null else categoria
                }
            )
        }
    }
}

@Composable
private fun ChipFiltro(texto: String, contagem: Int, ativo: Boolean, onClick: () -> Unit) {
    val cor = if (ativo) Cores.Ciano else Cores.TextoCinza
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(cor.copy(alpha = if (ativo) 0.16f else 0.05f))
            .border(1.dp, cor.copy(alpha = if (ativo) 0.9f else 0.3f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            texto,
            color = cor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(6.dp))
        Text("$contagem", color = cor.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}

/** Rótulo amigável para os tipos de ameaça usados como categoria de filtro. */
@Composable
private fun rotuloCategoria(tipo: String): String = when (tipo) {
    "SISTEMA" -> stringResource(R.string.sec_cat_sistema)
    "ACESSIBILIDADE" -> stringResource(R.string.sec_cat_acessibilidade)
    "CONFIGURAÇÃO" -> stringResource(R.string.sec_cat_configuracao)
    "PRIVACIDADE" -> stringResource(R.string.sec_cat_privacidade)
    "STALKERWARE" -> stringResource(R.string.sec_cat_stalkerware)
    "REDE" -> stringResource(R.string.sec_cat_rede)
    "APPS" -> stringResource(R.string.sec_cat_apps)
    "TELA" -> stringResource(R.string.sec_cat_tela)
    "LIXO" -> stringResource(R.string.sec_cat_lixo)
    else -> tipo
}

@Composable
private fun CartaoAmeaca(ameaca: Threat, onAbrir: () -> Unit, onCorrigir: () -> Unit) {
    val cor = Cores.corPorRisco(ameaca.risco)
    CartaoFuturista(onClick = onAbrir) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(ameaca.nome, color = Cores.TextoClaro, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(ameaca.descricao, color = Cores.TextoCinza, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.sec_threat_open),
                    color = Cores.Ciano,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.width(10.dp))
            SeloStatus("${ameaca.tipo} · ${ameaca.risco}", cor)
        }
        if (ameaca.acao != AcaoConfig.NENHUMA) {
            Spacer(Modifier.height(12.dp))
            BotaoNeon(
                texto = if (ameaca.acao == AcaoConfig.CACHE)
                    stringResource(R.string.sec_fix_clean)
                else stringResource(R.string.sec_fix),
                onClick = onCorrigir,
                cor = cor,
                modifier = Modifier.fillMaxWidth().height(44.dp)
            )
        }
    }
}
