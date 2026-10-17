package com.seguraturbo.app.ui.gameturbo

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seguraturbo.app.R
import com.seguraturbo.app.model.AcaoConfig
import com.seguraturbo.app.model.Game
import com.seguraturbo.app.model.PerfilDesempenho
import com.seguraturbo.app.ui.components.BarraProgressoNeon
import com.seguraturbo.app.ui.components.BotaoNeon
import com.seguraturbo.app.ui.components.CartaoFuturista
import com.seguraturbo.app.ui.components.HistoricoSecao
import com.seguraturbo.app.ui.components.SeloStatus
import com.seguraturbo.app.ui.components.TituloSecao
import com.seguraturbo.app.ui.theme.Cores
import com.seguraturbo.app.util.AcoesSistema

@Composable
fun GameTurboScreen(vm: GameTurboViewModel = viewModel()) {
    val contexto = LocalContext.current

    // Ao voltar das configurações, revalida bateria e acesso ao Não Perturbe.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                vm.atualizarBateria()
                vm.atualizarDnd()
                vm.atualizarTemperatura()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observador)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observador) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            CartaoFuturista {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(
                                if (vm.modoJogo) R.string.gt_game_mode_on else R.string.gt_game_mode_off
                            ),
                            color = if (vm.modoJogo) Cores.Verde else Cores.TextoClaro,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            stringResource(R.string.gt_mode_desc),
                            color = Cores.TextoCinza,
                            fontSize = 12.sp
                        )
                    }
                    Switch(checked = vm.modoJogo, onCheckedChange = { vm.definirModoJogo(it) })
                }
            }
        }

        item { CartaoPerfil(vm) }

        item { CartaoNaoPerturbe(vm) }

        item {
            BotaoNeon(
                texto = stringResource(R.string.gt_optimize),
                onClick = { vm.otimizar() },
                habilitado = !vm.otimizando,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item { CartaoBateria(vm) }

        item { CartaoTermico(vm) }

        if (vm.otimizando || vm.mensagem != null) {
            item {
                CartaoFuturista {
                    if (vm.otimizando) {
                        Text(
                            stringResource(R.string.gt_optimizing, vm.progresso),
                            color = Cores.Ciano,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        BarraProgressoNeon(progresso = vm.progresso / 100f)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 20.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                vm.mensagem ?: "",
                                color = Cores.Verde,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        item { TituloSecao(stringResource(R.string.gt_games_title)) }

        if (vm.jogos.isEmpty()) {
            item {
                CartaoFuturista {
                    Text(
                        stringResource(R.string.gt_no_games),
                        color = Cores.TextoCinza,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(vm.jogos) { jogo ->
                LinhaJogo(jogo, onAbrir = { AcoesSistema.abrirApp(contexto, jogo.pacote) })
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
private fun CartaoPerfil(vm: GameTurboViewModel) {
    CartaoFuturista {
        TituloSecao(stringResource(R.string.gt_perfil_titulo))
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PerfilOpcao(PerfilDesempenho.ECONOMIA, vm.perfil, vm::selecionarPerfil, Modifier.weight(1f))
            PerfilOpcao(PerfilDesempenho.EQUILIBRADO, vm.perfil, vm::selecionarPerfil, Modifier.weight(1f))
            PerfilOpcao(PerfilDesempenho.TURBO, vm.perfil, vm::selecionarPerfil, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Text(descricaoPerfil(vm.perfil), color = Cores.TextoCinza, fontSize = 12.sp)
    }
}

@Composable
private fun PerfilOpcao(
    perfil: PerfilDesempenho,
    atual: PerfilDesempenho,
    onSelecionar: (PerfilDesempenho) -> Unit,
    modifier: Modifier = Modifier
) {
    val selecionado = perfil == atual
    val cor = if (selecionado) Cores.Ciano else Cores.TextoCinza
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selecionado) Cores.Ciano.copy(alpha = 0.14f) else Cores.Fundo)
            .border(
                width = 1.dp,
                color = cor.copy(alpha = if (selecionado) 0.9f else 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelecionar(perfil) },
        contentAlignment = Alignment.Center
    ) {
        Text(rotuloPerfil(perfil), color = cor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun rotuloPerfil(p: PerfilDesempenho): String = stringResource(
    when (p) {
        PerfilDesempenho.ECONOMIA -> R.string.gt_perfil_economia
        PerfilDesempenho.EQUILIBRADO -> R.string.gt_perfil_equilibrado
        PerfilDesempenho.TURBO -> R.string.gt_perfil_turbo
    }
)

@Composable
private fun descricaoPerfil(p: PerfilDesempenho): String = stringResource(
    when (p) {
        PerfilDesempenho.ECONOMIA -> R.string.gt_perfil_economia_desc
        PerfilDesempenho.EQUILIBRADO -> R.string.gt_perfil_equilibrado_desc
        PerfilDesempenho.TURBO -> R.string.gt_perfil_turbo_desc
    }
)

@Composable
private fun CartaoNaoPerturbe(vm: GameTurboViewModel) {
    CartaoFuturista {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.gt_dnd_title),
                    color = Cores.TextoClaro,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (vm.dndAuto && !vm.dndAcesso) stringResource(R.string.gt_dnd_need_access)
                    else stringResource(R.string.gt_dnd_desc),
                    color = if (vm.dndAuto && !vm.dndAcesso) Cores.Amarelo else Cores.TextoCinza,
                    fontSize = 12.sp
                )
            }
            Switch(checked = vm.dndAuto, onCheckedChange = { vm.definirDndAuto(it) })
        }
    }
}

@Composable
private fun CartaoTermico(vm: GameTurboViewModel) {
    val t = vm.temperatura
    val cor = when {
        t == null -> Cores.TextoCinza
        t >= 42f -> Cores.Vermelho
        t >= 38f -> Cores.Amarelo
        else -> Cores.Verde
    }
    val estado = when {
        t == null -> stringResource(R.string.gt_termico_indisponivel)
        t >= 42f -> stringResource(R.string.gt_termico_aquecido)
        t >= 38f -> stringResource(R.string.gt_termico_morno)
        else -> stringResource(R.string.gt_termico_normal)
    }
    CartaoFuturista {
        TituloSecao(stringResource(R.string.gt_termico_titulo))
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🌡️", fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (t == null) stringResource(R.string.gt_termico_indisponivel)
                    else "${stringResource(R.string.gt_termico_valor, t)} · $estado",
                    color = cor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.gt_termico_desc),
                    color = Cores.TextoCinza,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CartaoBateria(vm: GameTurboViewModel) {
    val contexto = LocalContext.current
    val cor = if (vm.bateriaIsenta) Cores.Verde else Cores.Amarelo
    CartaoFuturista {
        TituloSecao(stringResource(R.string.gt_battery_title))
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🔋", fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(
                        if (vm.bateriaIsenta) R.string.gt_battery_ok else R.string.gt_battery_warn
                    ),
                    color = cor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.gt_battery_desc),
                    color = Cores.TextoCinza,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        BotaoNeon(
            texto = stringResource(R.string.gt_battery_btn),
            onClick = { AcoesSistema.abrirConfiguracao(contexto, AcaoConfig.BATERIA) },
            cor = Cores.Amarelo,
            modifier = Modifier.fillMaxWidth().height(44.dp)
        )
    }
}

@Composable
private fun LinhaJogo(jogo: Game, onAbrir: () -> Unit) {
    CartaoFuturista(onClick = onAbrir) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Cores.Fundo),
                contentAlignment = Alignment.Center
            ) {
                val icone = jogo.icone
                if (icone != null) {
                    Image(
                        bitmap = icone,
                        contentDescription = jogo.nome,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("🎮", fontSize = 22.sp)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(jogo.nome, color = Cores.TextoClaro, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(jogo.pacote, color = Cores.TextoCinza, fontSize = 11.sp)
            }
            if (jogo.otimizado) {
                Spacer(Modifier.width(8.dp))
                SeloStatus(stringResource(R.string.gt_optimized), Cores.Verde)
            }
            Spacer(Modifier.width(10.dp))
            Text("▶", color = Cores.Ciano, fontSize = 16.sp)
        }
    }
}
