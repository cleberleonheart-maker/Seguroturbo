package com.seguraturbo.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seguraturbo.app.R
import com.seguraturbo.app.ui.theme.Cores
import com.seguraturbo.app.util.Atualizacao

/**
 * Avisa que existe uma versão mais nova publicada no GitHub e baixa/instala o APK
 * direto pelo app (sem passar pelo navegador). Nada é instalado sem a confirmação
 * do usuário no instalador do sistema.
 */
@Composable
fun AtualizacaoDialog(
    atualizacao: Atualizacao,
    versaoAtual: String,
    instalavel: Boolean,
    baixando: Boolean,
    progresso: Int,
    onInstalar: () -> Unit,
    onDepois: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!baixando) onDepois() },
        containerColor = Cores.Superficie,
        titleContentColor = Cores.Ciano,
        textContentColor = Cores.TextoClaro,
        title = {
            Text(
                stringResource(R.string.upd_titulo),
                color = Cores.Ciano,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.upd_disponivel, atualizacao.versao),
                    color = Cores.TextoClaro,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.upd_versao_atual, versaoAtual),
                    color = Cores.TextoCinza,
                    fontSize = 12.sp
                )
                if (atualizacao.notas.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        atualizacao.notas.take(600),
                        color = Cores.TextoCinza,
                        fontSize = 13.sp
                    )
                }
                if (baixando) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (progresso < 0) stringResource(R.string.upd_baixando_indeterminado)
                        else stringResource(R.string.upd_baixando, progresso),
                        color = Cores.Ciano,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    if (progresso >= 0) {
                        LinearProgressIndicator(
                            progress = progresso / 100f,
                            modifier = Modifier.fillMaxWidth(),
                            color = Cores.Ciano,
                            trackColor = Cores.Borda
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Cores.Ciano,
                            trackColor = Cores.Borda
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onInstalar, enabled = !baixando) {
                Text(
                    stringResource(
                        if (instalavel) R.string.upd_instalar else R.string.upd_abrir_navegador
                    ),
                    color = Cores.Verde,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDepois, enabled = !baixando) {
                Text(stringResource(R.string.upd_depois), color = Cores.TextoCinza)
            }
        }
    )
}
