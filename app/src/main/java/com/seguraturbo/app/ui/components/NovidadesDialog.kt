package com.seguraturbo.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seguraturbo.app.R
import com.seguraturbo.app.model.VersaoChangelog
import com.seguraturbo.app.ui.theme.Cores

/**
 * Lista o que foi adicionado (Novo) e corrigido (Correções) em cada versão.
 * Usado ao atualizar o app e no botão "Novidades" das configurações.
 */
@Composable
fun NovidadesDialog(
    versoes: List<VersaoChangelog>,
    onFechar: () -> Unit
) {
    val ctx = LocalContext.current
    AlertDialog(
        onDismissRequest = onFechar,
        containerColor = Cores.Superficie,
        titleContentColor = Cores.Ciano,
        textContentColor = Cores.TextoClaro,
        title = {
            Text(
                stringResource(R.string.nov_title),
                color = Cores.Ciano,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                versoes.forEachIndexed { indice, versao ->
                    if (indice > 0) Spacer(Modifier.height(18.dp))
                    Text(
                        stringResource(R.string.nov_titulo, versao.versao),
                        color = Cores.Ciano,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val novidades = ctx.resources.getStringArray(versao.novidades)
                    val correcoes = ctx.resources.getStringArray(versao.correcoes)
                    if (novidades.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.nov_novidades),
                            color = Cores.Verde,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        novidades.forEach { item -> ItemLista(item, Cores.Verde) }
                    }
                    if (correcoes.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.nov_correcoes),
                            color = Cores.Ciano,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        correcoes.forEach { item -> ItemLista(item, Cores.Ciano) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onFechar) {
                Text(
                    stringResource(R.string.nov_fechar),
                    color = Cores.Verde,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun ItemLista(texto: String, cor: androidx.compose.ui.graphics.Color) {
    Row(modifier = Modifier.padding(top = 2.dp)) {
        Text("• ", color = cor, fontSize = 13.sp)
        Text(texto, color = Cores.TextoClaro, fontSize = 13.sp)
    }
}
