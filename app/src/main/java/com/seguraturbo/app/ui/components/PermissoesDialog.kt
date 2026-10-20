package com.seguraturbo.app.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seguraturbo.app.R
import com.seguraturbo.app.ui.theme.Cores
import com.seguraturbo.app.util.AppComPermissoes
import com.seguraturbo.app.util.AuditoriaPermissoes
import com.seguraturbo.app.util.InventarioCsv

/**
 * Diálogo de auditoria de permissões: mostra um resumo por permissão e a lista
 * de apps com acesso sensível concedido (câmera, microfone, localização, SMS,
 * contatos etc.). A análise roda fora da thread principal ao abrir o diálogo.
 */
@Composable
fun PermissoesDialog(onFechar: () -> Unit) {
    val ctx = LocalContext.current
    var carregando by remember { mutableStateOf(true) }
    var apps by remember { mutableStateOf<List<AppComPermissoes>>(emptyList()) }

    LaunchedEffect(Unit) {
        apps = AuditoriaPermissoes.listar(ctx)
        carregando = false
    }

    AlertDialog(
        onDismissRequest = onFechar,
        containerColor = Cores.Superficie,
        title = {
            Text(
                stringResource(R.string.perm_titulo),
                color = Cores.Ciano,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when {
                    carregando -> Text(
                        stringResource(R.string.perm_carregando),
                        color = Cores.TextoCinza,
                        fontSize = 14.sp
                    )
                    apps.isEmpty() -> Text(
                        stringResource(R.string.perm_vazio),
                        color = Cores.Verde,
                        fontSize = 14.sp
                    )
                    else -> {
                        Text(
                            stringResource(R.string.perm_resumo),
                            color = Cores.Ciano,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        AuditoriaPermissoes.resumo(apps).forEach { (grupo, quantidade) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(grupo.rotuloRes),
                                    color = Cores.TextoClaro,
                                    fontSize = 13.sp
                                )
                                SeloStatus(
                                    stringResource(R.string.perm_contagem, quantidade),
                                    Cores.Ciano
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.perm_apps),
                            color = Cores.Ciano,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        apps.forEach { app ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(
                                    app.nome,
                                    color = Cores.TextoClaro,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    app.grupos.joinToString(" · ") { ctx.getString(it.rotuloRes) },
                                    color = Cores.TextoCinza,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.perm_aviso),
                            color = Cores.TextoCinza,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!carregando && apps.isNotEmpty()) {
                    TextButton(onClick = {
                        InventarioCsv.compartilhar(
                            ctx,
                            apps,
                            rotulo = { ctx.getString(it.rotuloRes) },
                            texto = ctx.getString(R.string.perm_exportar_texto)
                        )
                    }) {
                        Text(
                            stringResource(R.string.perm_exportar),
                            color = Cores.Verde,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                TextButton(onClick = onFechar) {
                    Text(
                        stringResource(R.string.perm_fechar),
                        color = Cores.Ciano,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}
