package com.seguraturbo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seguraturbo.app.R
import com.seguraturbo.app.ui.theme.Cores
import com.seguraturbo.app.util.TesteToque
import kotlinx.coroutines.delay

/**
 * Teste de tela/touch: uma área onde o usuário toca o mais rápido possível durante
 * alguns segundos. Mede toques por segundo, resposta média e o maior número de
 * dedos simultâneos (multitouch). Tudo é capturado por eventos reais de ponteiro.
 */
@Composable
fun TesteToqueDialog(
    onFechar: () -> Unit,
    onConcluido: (TesteToque.Resultado) -> Unit
) {
    var fase by remember { mutableStateOf(Fase.INSTRUCAO) }
    var toques by remember { mutableStateOf(0) }
    var restante by remember { mutableStateOf(TesteToque.DURACAO_PADRAO_S) }
    var resultado by remember { mutableStateOf<TesteToque.Resultado?>(null) }

    val intervalos = remember { mutableListOf<Long>() }
    var ultimoToque by remember { mutableStateOf<Long?>(null) }
    var maxPontos by remember { mutableStateOf(0) }

    fun reiniciar() {
        toques = 0
        intervalos.clear()
        ultimoToque = null
        maxPontos = 0
        restante = TesteToque.DURACAO_PADRAO_S
        resultado = null
        fase = Fase.CONTANDO
    }

    // Contagem regressiva: ao terminar, calcula o resultado e avisa quem chamou.
    LaunchedEffect(fase) {
        if (fase != Fase.CONTANDO) return@LaunchedEffect
        val inicio = System.currentTimeMillis()
        while (true) {
            val passadoMs = System.currentTimeMillis() - inicio
            val faltando = TesteToque.DURACAO_PADRAO_S - (passadoMs / 1000).toInt()
            restante = faltando.coerceAtLeast(0)
            if (passadoMs >= TesteToque.DURACAO_PADRAO_S * 1000L) break
            delay(100)
        }
        val r = TesteToque.calcular(
            toques = toques,
            duracaoMs = TesteToque.DURACAO_PADRAO_S * 1000L,
            intervalosMs = intervalos.toList(),
            maxPontos = maxPontos
        )
        resultado = r
        fase = Fase.RESULTADO
        onConcluido(r)
    }

    AlertDialog(
        onDismissRequest = { if (fase != Fase.CONTANDO) onFechar() },
        containerColor = Cores.Superficie,
        title = {
            Text(
                stringResource(R.string.toque_titulo),
                color = Cores.Ciano,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                when (fase) {
                    Fase.INSTRUCAO -> Text(
                        stringResource(R.string.toque_instrucao, TesteToque.DURACAO_PADRAO_S),
                        color = Cores.TextoCinza,
                        fontSize = 14.sp
                    )

                    Fase.CONTANDO -> {
                        Text(
                            stringResource(R.string.toque_contando, restante),
                            color = Cores.Amarelo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Cores.Ciano.copy(alpha = 0.12f))
                                .pointerInput(fase) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val evento = awaitPointerEvent()
                                            val pressionados =
                                                evento.changes.count { it.pressed }
                                            if (pressionados > maxPontos) maxPontos = pressionados
                                            if (evento.type == PointerEventType.Press) {
                                                val agora = System.currentTimeMillis()
                                                ultimoToque?.let { intervalos += agora - it }
                                                ultimoToque = agora
                                                toques++
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.toque_toque_aqui),
                                color = Cores.Ciano,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.toque_toques_parcial, toques),
                            color = Cores.TextoClaro,
                            fontSize = 13.sp
                        )
                    }

                    Fase.RESULTADO -> resultado?.let { r ->
                        Text(
                            stringResource(R.string.toque_toques, r.toques, TesteToque.DURACAO_PADRAO_S),
                            color = Cores.Verde,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(10.dp))
                        LinhaResultado(
                            stringResource(R.string.toque_por_segundo),
                            String.format(java.util.Locale.getDefault(), "%.1f", r.toquesPorSegundo)
                        )
                        LinhaResultado(
                            stringResource(R.string.toque_resposta),
                            stringResource(R.string.toque_resposta_valor, r.respostaMediaMs)
                        )
                        LinhaResultado(
                            stringResource(R.string.toque_multitouch),
                            r.maxPontos.toString()
                        )
                    }
                }
            }
        },
        confirmButton = {
            when (fase) {
                Fase.INSTRUCAO -> TextButton(onClick = { reiniciar() }) {
                    Text(stringResource(R.string.toque_iniciar), color = Cores.Ciano, fontWeight = FontWeight.Bold)
                }
                Fase.RESULTADO -> TextButton(onClick = { reiniciar() }) {
                    Text(stringResource(R.string.toque_refazer), color = Cores.Ciano, fontWeight = FontWeight.Bold)
                }
                Fase.CONTANDO -> Unit
            }
        },
        dismissButton = {
            if (fase != Fase.CONTANDO) {
                TextButton(onClick = onFechar) {
                    Text(stringResource(R.string.toque_fechar), color = Cores.TextoCinza)
                }
            }
        }
    )
}

private enum class Fase { INSTRUCAO, CONTANDO, RESULTADO }

@Composable
private fun LinhaResultado(rotulo: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(rotulo, color = Cores.TextoCinza, fontSize = 13.sp)
        Text(valor, color = Cores.TextoClaro, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
