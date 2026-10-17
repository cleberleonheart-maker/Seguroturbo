package com.seguraturbo.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
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
 * Avisa que existe uma versão mais nova publicada no GitHub e oferece o
 * download manual. Nada é instalado sem a confirmação do usuário.
 */
@Composable
fun AtualizacaoDialog(
    atualizacao: Atualizacao,
    versaoAtual: String,
    onBaixar: () -> Unit,
    onDepois: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDepois,
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
            }
        },
        confirmButton = {
            TextButton(onClick = onBaixar) {
                Text(
                    stringResource(R.string.upd_baixar),
                    color = Cores.Verde,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDepois) {
                Text(stringResource(R.string.upd_depois), color = Cores.TextoCinza)
            }
        }
    )
}
