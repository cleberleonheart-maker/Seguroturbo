package com.seguraturbo.app.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seguraturbo.app.ui.theme.Cores
import com.seguraturbo.app.util.TesteConsumoBateria
import kotlinx.coroutines.delay

@Composable
fun TesteBateriaDialog(onDismiss: () -> Unit) {
    val contexto = LocalContext.current
    var rodando by remember { mutableStateOf(false) }
    var segundos by remember { mutableStateOf(0) }
    var nivelInicio by remember { mutableStateOf(TesteConsumoBateria.nivelAtual(contexto)) }
    var nivelAtual by remember { mutableStateOf(nivelInicio) }
    var finalizado by remember { mutableStateOf(false) }
    var drenagem by remember { mutableStateOf(0) }

    LaunchedEffect(rodando) {
        if (rodando && !finalizado) {
            val inicio = System.currentTimeMillis()
            nivelInicio = TesteConsumoBateria.nivelAtual(contexto)
            nivelAtual = nivelInicio
            while (segundos < 600 && rodando) { // 10 minutos por padrão
                delay(1000)
                segundos++
                nivelAtual = TesteConsumoBateria.nivelAtual(contexto)
            }
            val fim = System.currentTimeMillis()
            val fimNivel = TesteConsumoBateria.nivelAtual(contexto)
            val dren = if (nivelInicio >= fimNivel) nivelInicio - fimNivel else 0
            drenagem = dren
            TesteConsumoBateria.salvar(
                contexto,
                TesteConsumoBateria.Resultado(
                    inicio = inicio,
                    fim = fim,
                    nivelInicio = nivelInicio,
                    nivelFim = fimNivel,
                    drenagem = dren,
                    duracaoMin = (fim - inicio) / 60000L
                )
            )
            finalizado = true
            rodando = false
        }
    }

    AlertDialog(
        onDismissRequest = { if (!rodando) onDismiss() },
        containerColor = Cores.Superficie,
        title = {
            Text("Teste de consumo de bateria", color = Cores.TextoClaro, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!rodando && !finalizado) {
                    Text(
                        "Mede a drenagem da bateria em 10 minutos em uso normal. Deixe o app aberto durante o teste.",
                        color = Cores.TextoCinza,
                        fontSize = 12.sp
                    )
                    Text("Nível atual: $nivelAtual%", color = Cores.TextoClaro, fontSize = 13.sp)
                } else if (rodando) {
                    Text("Rodando... ${segundos/60}:${String.format("%02d", segundos%60)} / 10:00", color = Cores.Ciano, fontSize = 14.sp)
                    Text("Nível: $nivelAtual% (início $nivelInicio%)", color = Cores.TextoCinza, fontSize = 12.sp)
                } else {
                    Text("Concluído", color = Cores.Verde, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Duração: ${segundos/60} min ${segundos%60} s", color = Cores.TextoClaro, fontSize = 13.sp)
                    Text("Drenagem: $drenagem% (de $nivelInicio% para $nivelAtual%)", color = if (drenagem > 10) Cores.Amarelo else Cores.TextoClaro, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            if (!rodando && !finalizado) {
                TextButton(onClick = { rodando = true; segundos = 0; nivelInicio = TesteConsumoBateria.nivelAtual(contexto) }) {
                    Text("Iniciar", color = Cores.Ciano)
                }
            } else if (finalizado) {
                TextButton(onClick = onDismiss) {
                    Text("Fechar", color = Cores.Ciano)
                }
            }
        },
        dismissButton = {
            if (rodando) {
                TextButton(onClick = { rodando = false; finalizado = true }) {
                    Text("Parar", color = Cores.Amarelo)
                }
            } else if (!finalizado) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = Cores.TextoCinza)
                }
            }
        }
    )
}
