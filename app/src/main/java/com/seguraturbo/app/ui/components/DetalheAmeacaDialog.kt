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
import com.seguraturbo.app.model.AcaoConfig
import com.seguraturbo.app.model.Threat
import com.seguraturbo.app.ui.theme.Cores

/**
 * Diálogo educativo que explica um alerta de segurança: o que significa e como
 * corrigir. Reúne a descrição real do alerta e uma recomendação por tipo de ação.
 */
@Composable
fun DetalheAmeacaDialog(
    ameaca: Threat,
    onCorrigir: () -> Unit,
    onFechar: () -> Unit
) {
    val cor = Cores.corPorRisco(ameaca.risco)
    AlertDialog(
        onDismissRequest = onFechar,
        containerColor = Cores.Superficie,
        title = {
            Text(ameaca.nome, color = Cores.TextoClaro, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                SeloStatus("${ameaca.tipo} · ${ameaca.risco}", cor)
                Spacer(Modifier.height(14.dp))
                Text(
                    stringResource(R.string.sec_detail_what),
                    color = Cores.Ciano,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(ameaca.descricao, color = Cores.TextoClaro, fontSize = 14.sp)
                Spacer(Modifier.height(14.dp))
                Text(
                    stringResource(R.string.sec_detail_fix),
                    color = Cores.Ciano,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(recomendacao(ameaca.acao), color = Cores.TextoCinza, fontSize = 13.sp)
            }
        },
        confirmButton = {
            if (ameaca.acao != AcaoConfig.NENHUMA) {
                TextButton(onClick = onCorrigir) {
                    Text(
                        stringResource(
                            if (ameaca.acao == AcaoConfig.CACHE) R.string.sec_fix_clean
                            else R.string.sec_fix
                        ),
                        color = cor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onFechar) {
                Text(stringResource(R.string.sec_detail_close), color = Cores.TextoCinza)
            }
        }
    )
}

@Composable
private fun recomendacao(acao: AcaoConfig): String = stringResource(
    when (acao) {
        AcaoConfig.SEGURANCA -> R.string.sec_detail_seguranca
        AcaoConfig.FONTES_DESCONHECIDAS -> R.string.sec_detail_fontes
        AcaoConfig.ACESSIBILIDADE -> R.string.sec_detail_acesso
        AcaoConfig.OPCOES_DESENVOLVEDOR -> R.string.sec_detail_dev
        AcaoConfig.CACHE -> R.string.sec_detail_cache
        AcaoConfig.BATERIA -> R.string.sec_detail_bateria
        AcaoConfig.ATUALIZACAO_SISTEMA -> R.string.sec_detail_atualizacao
        AcaoConfig.NENHUMA -> R.string.sec_detail_nenhuma
    }
)
