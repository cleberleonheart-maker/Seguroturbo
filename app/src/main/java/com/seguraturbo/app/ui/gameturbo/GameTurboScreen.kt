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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
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
import com.seguraturbo.app.ui.components.HoraDialog
import com.seguraturbo.app.ui.components.LimpezaRamDialog
import com.seguraturbo.app.ui.components.SeloStatus
import com.seguraturbo.app.ui.components.TituloSecao
import com.seguraturbo.app.ui.theme.Cores
import com.seguraturbo.app.service.PermissaoSobreposicao
import com.seguraturbo.app.service.ServicoBarraJogo
import com.seguraturbo.app.util.AcoesSistema
import com.seguraturbo.app.util.AtalhoAcao
import com.seguraturbo.app.util.AtalhoPendente
import com.seguraturbo.app.util.BloqueioTela
import android.app.Activity
import android.widget.Toast

@Composable
fun GameTurboScreen(vm: GameTurboViewModel = viewModel()) {
    val contexto = LocalContext.current

    // Atalho "Limpar RAM" do ícone: abre o diálogo de limpeza ao entrar na tela.
    LaunchedEffect(Unit) {
        if (AtalhoPendente.consumir() == AtalhoAcao.LIMPAR_RAM) {
            vm.atualizarRam()
            vm.mostrarLimpezaRam = true
        }
    }

    // Ao voltar das configurações, revalida bateria e acesso ao Não Perturbe.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                vm.atualizarBateria()
                vm.atualizarDnd()
                vm.atualizarTemperatura()
                vm.atualizarRam()
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

        item { CartaoBarraJogo(contexto) }
        item { CartaoTravaTela(vm) }
        item { CartaoSilenciarMidia(vm) }
        item { CartaoAutoLimparRam(vm) }
        item { CartaoSessaoFoco(vm) }
        item { CartaoTurboAgendado(vm) }

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

        item { CartaoRam(vm) }

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
                LinhaJogo(jogo, onAbrir = {
                    val fixar = vm.travaTelaJogo && vm.travaTelaSistema && contexto is Activity
                    if (fixar) BloqueioTela.fixar(contexto as Activity)
                    if (PermissaoSobreposicao.podeDesenhar(contexto)) {

                        ServicoBarraJogo.iniciar(contexto, jogo.pacote)
                    } else {
                        Toast.makeText(
                            contexto,
                            R.string.barra_precisa_acesso,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    val abriu = AcoesSistema.abrirApp(contexto, jogo.pacote, noMesmoTask = fixar)
                    if (!abriu && fixar) BloqueioTela.desfixar(contexto as Activity)
                })
            }
        }

        item {
            CartaoFuturista {
                HistoricoSecao(vm.historico, onLimpar = { vm.limparHistorico() })
            }
        }
    }

    if (vm.mostrarLimpezaRam) {
        LimpezaRamDialog(
            onFechar = { vm.mostrarLimpezaRam = false },
            onLiberou = { quantidade ->
                vm.mostrarLimpezaRam = false
                vm.memoriasEncerradas(quantidade)
            }
        )
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
private fun CartaoBarraJogo(contexto: android.content.Context) {
    CartaoFuturista {
        TituloSecao(stringResource(R.string.barra_titulo))
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.barra_desc),
            color = Cores.TextoCinza,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BotaoNeon(
                texto = stringResource(R.string.barra_mostrar),
                onClick = {
                    if (PermissaoSobreposicao.podeDesenhar(contexto)) {
                        ServicoBarraJogo.iniciar(contexto, null)
                    } else {
                        PermissaoSobreposicao.pedir(contexto)
                    }
                },
                modifier = Modifier.weight(1f)
            )
            BotaoNeon(
                texto = stringResource(R.string.barra_ocultar),
                onClick = { ServicoBarraJogo.parar(contexto) },
                modifier = Modifier.weight(1f)
            )
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
private fun CartaoRam(vm: GameTurboViewModel) {
    val m = vm.memoria
    val cor = when {
        m.percentualUsado >= 85 -> Cores.Vermelho
        m.percentualUsado >= 70 -> Cores.Amarelo
        else -> Cores.Verde
    }
    CartaoFuturista {
        TituloSecao(stringResource(R.string.ram_titulo))
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.ram_desc),
            color = Cores.TextoCinza,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🧠", fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.ram_livre, m.livreMb, m.totalMb),
                    color = cor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.ram_uso, m.percentualUsado),
                    color = Cores.TextoCinza,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        BarraProgressoNeon(progresso = m.percentualUsado / 100f)
        Spacer(Modifier.height(12.dp))
        BotaoNeon(
            texto = stringResource(R.string.ram_abrir),
            onClick = { vm.mostrarLimpezaRam = true },
            modifier = Modifier.fillMaxWidth().height(44.dp)
        )
    }
}

@Composable
private fun CartaoBateria(vm: GameTurboViewModel) {    val contexto = LocalContext.current
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

@Composable
private fun CartaoTravaTela(vm: GameTurboViewModel) {
    CartaoFuturista {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.trava_titulo),
                    color = Cores.TextoClaro,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                val avisar = vm.travaTelaJogo && !vm.travaTelaSistema
                Text(
                    if (avisar) stringResource(R.string.trava_sistema_desativado)
                    else stringResource(R.string.trava_desc),
                    color = if (avisar) Cores.Amarelo else Cores.TextoCinza,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = vm.travaTelaJogo,
                onCheckedChange = { vm.definirTravaTelaJogo(it) }
            )
        }
    }
}

@Composable
private fun CartaoSilenciarMidia(vm: GameTurboViewModel) {
    CartaoFuturista {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.som_titulo),
                    color = Cores.TextoClaro,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.som_desc),
                    color = Cores.TextoCinza,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = vm.silenciarMidia,
                onCheckedChange = { vm.definirSilenciarMidia(it) }
            )
        }
    }
}

@Composable
private fun CartaoAutoLimparRam(vm: GameTurboViewModel) {
    CartaoFuturista {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.gt_auto_limpar_ram),
                    color = Cores.TextoClaro,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.ram_desc),
                    color = Cores.TextoCinza,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = vm.autoLimparRamJogo,
                onCheckedChange = { vm.definirAutoLimparRamJogo(it) }
            )
        }
    }
}

@Composable
private fun CartaoTurboAgendado(vm: GameTurboViewModel) {
    var editandoLigar by remember { mutableStateOf(false) }
    var editandoDesligar by remember { mutableStateOf(false) }

    fun fmt(h: Int, m: Int) = String.format(java.util.Locale.getDefault(), "%02d:%02d", h, m)

    CartaoFuturista {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Turbo agendado",
                    color = Cores.TextoClaro,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Liga o Modo Jogo no horário escolhido e desliga no fim.",
                    color = Cores.TextoCinza,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = vm.turboAgendadoAtivo,
                onCheckedChange = { vm.definirTurboAgendadoAtivo(it) }
            )
        }
        if (vm.turboAgendadoAtivo) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotaoNeon(
                    texto = "Ligar ${fmt(vm.turboHoraLigar, vm.turboMinLigar)}",
                    onClick = { editandoLigar = true },
                    cor = Cores.Verde,
                    modifier = Modifier.weight(1f)
                )
                BotaoNeon(
                    texto = "Desligar ${fmt(vm.turboHoraDesligar, vm.turboMinDesligar)}",
                    onClick = { editandoDesligar = true },
                    cor = Cores.Amarelo,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (editandoLigar) {
        HoraDialog(
            titulo = "Hora de ligar o Modo Jogo",
            horaInicial = vm.turboHoraLigar,
            minutoInicial = vm.turboMinLigar,
            onConfirmar = { h, m ->
                vm.definirTurboHoraLigar(h, m)
                editandoLigar = false
            },
            onDismiss = { editandoLigar = false }
        )
    }
    if (editandoDesligar) {
        HoraDialog(
            titulo = "Hora de desligar o Modo Jogo",
            horaInicial = vm.turboHoraDesligar,
            minutoInicial = vm.turboMinDesligar,
            onConfirmar = { h, m ->
                vm.definirTurboHoraDesligar(h, m)
                editandoDesligar = false
            },
            onDismiss = { editandoDesligar = false }
        )
    }
}

@Composable
private fun CartaoSessaoFoco(vm: GameTurboViewModel) {
    CartaoFuturista {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.gt_sessao_foco),
                    color = Cores.TextoClaro,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (vm.sessaoFocoAtiva) "${vm.sessaoFocoRestanteSeg / 60}m ${(vm.sessaoFocoRestanteSeg % 60)}s"
                    else stringResource(R.string.gt_sessao_foco_desc),
                    color = if (vm.sessaoFocoAtiva) Cores.Verde else Cores.TextoCinza,
                    fontSize = 12.sp
                )
            }
            BotaoNeon(
                texto = stringResource(if (vm.sessaoFocoAtiva) R.string.gt_sessao_foco_encerrar else R.string.gt_sessao_foco_iniciar),
                onClick = { if (vm.sessaoFocoAtiva) vm.encerrarSessaoFoco() else vm.iniciarSessaoFoco() },
                habilitado = true
            )
        }
    }
}
