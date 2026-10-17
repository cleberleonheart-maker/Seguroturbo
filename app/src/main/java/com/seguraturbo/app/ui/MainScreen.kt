@file:OptIn(ExperimentalMaterial3Api::class)

package com.seguraturbo.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seguraturbo.app.R
import com.seguraturbo.app.ui.benchmark.BenchmarkScreen
import com.seguraturbo.app.ui.components.ConfiguracoesDialog
import com.seguraturbo.app.ui.gameturbo.GameTurboScreen
import com.seguraturbo.app.ui.seguranca.SegurancaScreen
import com.seguraturbo.app.ui.theme.Cores

enum class TelaNavegacao(val rotuloRes: Int, val iconeRes: Int) {
    SEGURANCA(R.string.nav_seguranca, R.drawable.ic_security),
    GAME_TURBO(R.string.nav_game_turbo, R.drawable.ic_game_turbo),
    BENCHMARK(R.string.nav_benchmark, R.drawable.ic_benchmark)
}

@Composable
fun MainScreen(abaInicial: Int = 0) {
    var indice by rememberSaveable { mutableStateOf(abaInicial) }
    var mostrarConfig by rememberSaveable { mutableStateOf(false) }
    val telas = TelaNavegacao.values()

    // Atalhos do ícone do app podem trocar a aba depois que a tela já está aberta.
    LaunchedEffect(abaInicial) {
        if (abaInicial in telas.indices) indice = abaInicial
    }

    if (mostrarConfig) {
        ConfiguracoesDialog(onFechar = { mostrarConfig = false })
    }

    Scaffold(
        containerColor = Cores.Fundo,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        color = Cores.Ciano,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                actions = {
                    val descConfig = stringResource(R.string.cfg_title)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .semantics { contentDescription = descConfig }
                            .clickable { mostrarConfig = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚙️", fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Cores.FundoSuave
                )
            )
        },
        bottomBar = {
            BarraNavegacao(telas, indice) { indice = it }
        }
    ) { padding ->
        AnimatedContent(
            targetState = telas[indice],
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            label = "navegacao"
        ) { tela ->
            when (tela) {
                TelaNavegacao.SEGURANCA -> SegurancaScreen()
                TelaNavegacao.GAME_TURBO -> GameTurboScreen()
                TelaNavegacao.BENCHMARK -> BenchmarkScreen()
            }
        }
    }
}

@Composable
private fun BarraNavegacao(
    telas: Array<TelaNavegacao>,
    selecionada: Int,
    onSelecionar: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cores.FundoSuave)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        telas.forEachIndexed { index, tela ->
            val ativo = index == selecionada
            val cor = if (ativo) Cores.Ciano else Cores.TextoClaro
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (ativo) Cores.Ciano.copy(alpha = 0.14f) else Color.Transparent)
                    .selectable(
                        selected = ativo,
                        role = Role.Tab,
                        onClick = { onSelecionar(index) }
                    )
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(tela.iconeRes),
                    contentDescription = stringResource(tela.rotuloRes),
                    tint = if (ativo) Cores.Ciano else Cores.TextoCinza,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(tela.rotuloRes),
                    color = cor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}
