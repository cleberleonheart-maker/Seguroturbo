package com.seguraturbo.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seguraturbo.app.model.Game
import com.seguraturbo.app.model.PerfilDesempenho
import com.seguraturbo.app.model.PerfilJogo
import com.seguraturbo.app.ui.theme.Cores

@Composable
fun PerfilJogoDialog(
    jogo: Game,
    perfilAtual: PerfilJogo?,
    onSalvar: (PerfilJogo) -> Unit,
    onRemover: () -> Unit,
    onDismiss: () -> Unit
) {
    var perfil by remember { mutableStateOf(perfilAtual?.perfil ?: PerfilDesempenho.EQUILIBRADO) }
    var naoPerturbe by remember { mutableStateOf(perfilAtual?.naoPerturbe ?: false) }
    var travaTela by remember { mutableStateOf(perfilAtual?.travaTela ?: false) }
    var barraJogo by remember { mutableStateOf(perfilAtual?.barraJogo ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Cores.Superficie,
        title = {
            Text(jogo.nome, color = Cores.TextoClaro, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Perfil por jogo", color = Cores.Ciano, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Perfil de desempenho", color = Cores.TextoCinza, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(PerfilDesempenho.ECONOMIA, PerfilDesempenho.EQUILIBRADO, PerfilDesempenho.TURBO).forEach { p ->
                        val ativo = perfil == p
                        TextButton(onClick = { perfil = p }) {
                            Text(
                                when (p) {
                                    PerfilDesempenho.ECONOMIA -> "Economia"
                                    PerfilDesempenho.EQUILIBRADO -> "Equilibrado"
                                    PerfilDesempenho.TURBO -> "Turbo"
                                },
                                color = if (ativo) Cores.Ciano else Cores.TextoCinza
                            )
                        }
                    }
                }
                LinhaToggle("Não Perturbe ao abrir", naoPerturbe) { naoPerturbe = it }
                LinhaToggle("Bloquear tela (screen pinning)", travaTela) { travaTela = it }
                LinhaToggle("Barra flutuante do jogo", barraJogo) { barraJogo = it }
                if (perfilAtual != null) {
                    TextButton(onClick = onRemover) {
                        Text("Remover perfil personalizado", color = Cores.Amarelo)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSalvar(
                    PerfilJogo(
                        pacote = jogo.pacote,
                        nome = jogo.nome,
                        perfil = perfil,
                        naoPerturbe = naoPerturbe,
                        travaTela = travaTela,
                        barraJogo = barraJogo
                    )
                )
            }) {
                Text("Salvar", color = Cores.Ciano)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Cores.TextoCinza)
            }
        }
    )
}

@Composable
private fun LinhaToggle(texto: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(texto, color = Cores.TextoClaro, fontSize = 13.sp)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
