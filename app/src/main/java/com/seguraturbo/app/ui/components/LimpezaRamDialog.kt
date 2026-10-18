package com.seguraturbo.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seguraturbo.app.R
import com.seguraturbo.app.ui.theme.Cores
import com.seguraturbo.app.util.GerenciadorRam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Diálogo de limpeza de RAM: lista os apps que podem ser encerrados, deixa o
 * usuário escolher e encerra os processos em segundo plano dos selecionados.
 * A listagem e o encerramento rodam fora da thread principal.
 */
@Composable
fun LimpezaRamDialog(
    onFechar: () -> Unit,
    onLiberou: (liberados: Int) -> Unit
) {
    val ctx = LocalContext.current
    var carregando by remember { mutableStateOf(true) }
    var apps by remember { mutableStateOf<List<GerenciadorRam.AppAlvo>>(emptyList()) }
    var selecionados by remember { mutableStateOf(emptySet<String>()) }
    var liberando by remember { mutableStateOf(false) }
    val escopo = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) { GerenciadorRam.appsFechaveis(ctx) }
        carregando = false
    }

    AlertDialog(
        onDismissRequest = { if (!liberando) onFechar() },
        containerColor = Cores.Superficie,
        title = {
            Text(
                stringResource(R.string.ram_titulo),
                color = Cores.Ciano,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    stringResource(R.string.ram_selecione),
                    color = Cores.TextoCinza,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(8.dp))
                when {
                    carregando -> Text(
                        stringResource(R.string.ram_carregando),
                        color = Cores.TextoCinza,
                        fontSize = 14.sp
                    )
                    apps.isEmpty() -> Text(
                        stringResource(R.string.ram_vazio),
                        color = Cores.TextoCinza,
                        fontSize = 14.sp
                    )
                    else -> apps.forEach { app ->
                        val marcado = app.pacote in selecionados
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selecionados = if (marcado) {
                                        selecionados - app.pacote
                                    } else {
                                        selecionados + app.pacote
                                    }
                                }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = marcado,
                                onCheckedChange = { ativo ->
                                    selecionados = if (ativo) {
                                        selecionados + app.pacote
                                    } else {
                                        selecionados - app.pacote
                                    }
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    app.nome,
                                    color = Cores.TextoClaro,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(app.pacote, color = Cores.TextoCinza, fontSize = 11.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.ram_aviso),
                    color = Cores.TextoCinza,
                    fontSize = 10.sp
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = selecionados.isNotEmpty() && !liberando,
                onClick = {
                    liberando = true
                    val escolhidos = selecionados.toList()
                    // Encerra fora da thread principal e informa o total.
                    escopo.launch {
                        val total = withContext(Dispatchers.IO) {
                            GerenciadorRam.liberar(ctx, escolhidos)
                        }
                        liberando = false
                        onLiberou(total)
                    }
                }
            ) {
                Text(
                    stringResource(R.string.ram_liberar, selecionados.size),
                    color = if (selecionados.isEmpty()) Cores.TextoCinza else Cores.Ciano,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(enabled = !liberando, onClick = onFechar) {
                Text(stringResource(R.string.ram_fechar), color = Cores.TextoCinza)
            }
        }
    )
}
