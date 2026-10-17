package com.seguraturbo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.seguraturbo.app.R
import com.seguraturbo.app.model.TemaModo
import com.seguraturbo.app.ui.theme.Aparencia
import com.seguraturbo.app.ui.theme.Cores
import com.seguraturbo.app.util.Agendador
import com.seguraturbo.app.util.AgendadorAtualizacao
import com.seguraturbo.app.util.AssinaturaApp
import com.seguraturbo.app.util.Backup
import com.seguraturbo.app.util.Notificacoes
import com.seguraturbo.app.util.Preferencias
import kotlin.math.abs

/**
 * Diálogo de configurações rápidas: tema claro/escuro, tamanho da fonte,
 * lembrete de verificação e checagem manual de atualizações.
 * As mudanças são aplicadas na hora e gravadas nas preferências.
 */
@Composable
fun ConfiguracoesDialog(
    versaoAtual: String,
    checando: Boolean,
    onVerificarAtualizacao: () -> Unit,
    onFechar: () -> Unit
) {
    val ctx = LocalContext.current
    var modo by remember { mutableStateOf(Aparencia.temaModo) }
    var escala by remember { mutableStateOf(Aparencia.escalaFonte) }
    var lembrete by remember { mutableStateOf(Preferencias.lembreteAtivo(ctx)) }
    var atualizacaoAuto by remember { mutableStateOf(Preferencias.atualizacaoAutomatica(ctx)) }
    val assinatura by remember { mutableStateOf(AssinaturaApp.sha256(ctx)) }
    val sistemaEscuro = isSystemInDarkTheme()

    val pedirNotificacao = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* o lembrete já foi ativado; a permissão apenas libera a notificação */ }

    fun reaplicarPreferencias() {
        escala = Preferencias.escalaFonte(ctx)
        Aparencia.escalaFonte = escala
        modo = Preferencias.temaModo(ctx)
        Aparencia.temaModo = modo
        Cores.definirTema(
            when (modo) {
                TemaModo.SISTEMA -> sistemaEscuro
                TemaModo.CLARO -> false
                TemaModo.ESCURO -> true
            }
        )
        lembrete = Preferencias.lembreteAtivo(ctx)
        atualizacaoAuto = Preferencias.atualizacaoAutomatica(ctx)
        Notificacoes.criarCanal(ctx)
        if (lembrete) Agendador.agendar(ctx) else Agendador.cancelar(ctx)
        if (atualizacaoAuto) AgendadorAtualizacao.agendar(ctx) else AgendadorAtualizacao.cancelar(ctx)
    }

    val exportarBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val ok = runCatching {
            ctx.contentResolver.openOutputStream(uri)?.use { saida ->
                saida.write(Backup.montar(ctx).toByteArray())
            } != null
        }.getOrDefault(false)
        Toast.makeText(
            ctx,
            if (ok) R.string.backup_ok else R.string.backup_erro,
            Toast.LENGTH_SHORT
        ).show()
    }

    val importarBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val ok = runCatching {
            ctx.contentResolver.openInputStream(uri)?.use { entrada ->
                Backup.aplicar(ctx, entrada.readBytes().decodeToString())
            } ?: false
        }.getOrDefault(false)
        if (ok) reaplicarPreferencias()
        Toast.makeText(
            ctx,
            if (ok) R.string.backup_restaurado else R.string.backup_erro,
            Toast.LENGTH_SHORT
        ).show()
    }

    val opcoes = listOf(
        R.string.cfg_font_small to 0.85f,
        R.string.cfg_font_normal to 1f,
        R.string.cfg_font_large to 1.3f
    )

    val opcoesTema = listOf(
        TemaModo.SISTEMA to R.string.cfg_tema_sistema,
        TemaModo.CLARO to R.string.cfg_tema_claro,
        TemaModo.ESCURO to R.string.cfg_tema_escuro
    )

    AlertDialog(
        onDismissRequest = onFechar,
        containerColor = Cores.Superficie,
        titleContentColor = Cores.Ciano,
        textContentColor = Cores.TextoClaro,
        title = {
            Text(
                stringResource(R.string.cfg_title),
                color = Cores.Ciano,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    stringResource(R.string.cfg_dark),
                    color = Cores.TextoClaro,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.cfg_dark_desc),
                    color = Cores.TextoCinza,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    opcoesTema.forEach { (opcao, rotuloRes) ->
                        val selecionado = modo == opcao
                        val cor = if (selecionado) Cores.Ciano else Cores.TextoCinza
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(cor.copy(alpha = if (selecionado) 0.16f else 0.05f))
                                .border(
                                    width = 1.dp,
                                    color = cor.copy(alpha = if (selecionado) 0.9f else 0.3f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    modo = opcao
                                    Aparencia.temaModo = opcao
                                    Preferencias.definirTemaModo(ctx, opcao)
                                    Cores.definirTema(
                                        when (opcao) {
                                            TemaModo.SISTEMA -> sistemaEscuro
                                            TemaModo.CLARO -> false
                                            TemaModo.ESCURO -> true
                                        }
                                    )
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(rotuloRes),
                                color = cor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    stringResource(R.string.cfg_font),
                    color = Cores.TextoClaro,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.cfg_font_desc),
                    color = Cores.TextoCinza,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    opcoes.forEach { (rotuloRes, valor) ->
                        val selecionado = abs(escala - valor) < 0.01f
                        val cor = if (selecionado) Cores.Ciano else Cores.TextoCinza
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(cor.copy(alpha = if (selecionado) 0.16f else 0.05f))
                                .border(
                                    width = 1.dp,
                                    color = cor.copy(alpha = if (selecionado) 0.9f else 0.3f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    escala = valor
                                    Aparencia.escalaFonte = valor
                                    Preferencias.definirEscalaFonte(ctx, valor)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(rotuloRes),
                                color = cor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.cfg_lembrete),
                            color = Cores.TextoClaro,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(R.string.cfg_lembrete_desc),
                            color = Cores.TextoCinza,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = lembrete,
                        onCheckedChange = { novo ->
                            lembrete = novo
                            Preferencias.definirLembreteAtivo(ctx, novo)
                            if (novo) {
                                Notificacoes.criarCanal(ctx)
                                Agendador.agendar(ctx)
                                val precisaPermissao = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(
                                        ctx, Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                if (precisaPermissao) {
                                    pedirNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                Agendador.cancelar(ctx)
                            }
                        }
                    )
                }

                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.upd_verificar),
                            color = Cores.TextoClaro,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(R.string.upd_versao_atual, versaoAtual),
                            color = Cores.TextoCinza,
                            fontSize = 12.sp
                        )
                    }
                    TextButton(
                        onClick = onVerificarAtualizacao,
                        enabled = !checando
                    ) {
                        Text(
                            stringResource(
                                if (checando) R.string.upd_verificando
                                else R.string.upd_verificar_btn
                            ),
                            color = if (checando) Cores.TextoCinza else Cores.Ciano,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.cfg_upd_auto),
                            color = Cores.TextoClaro,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(R.string.cfg_upd_auto_desc),
                            color = Cores.TextoCinza,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = atualizacaoAuto,
                        onCheckedChange = { novo ->
                            atualizacaoAuto = novo
                            Preferencias.definirAtualizacaoAutomatica(ctx, novo)
                            if (novo) {
                                Notificacoes.criarCanalAtualizacao(ctx)
                                AgendadorAtualizacao.agendar(ctx)
                                val precisaPermissao =
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        ContextCompat.checkSelfPermission(
                                            ctx, Manifest.permission.POST_NOTIFICATIONS
                                        ) != PackageManager.PERMISSION_GRANTED
                                if (precisaPermissao) {
                                    pedirNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                AgendadorAtualizacao.cancelar(ctx)
                            }
                        }
                    )
                }

                if (assinatura != null) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        stringResource(R.string.cfg_assinatura),
                        color = Cores.TextoClaro,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        stringResource(R.string.cfg_assinatura_desc),
                        color = Cores.TextoCinza,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        assinatura!!,
                        color = Cores.Ciano,
                        fontSize = 10.sp
                    )
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    stringResource(R.string.cfg_backup),
                    color = Cores.TextoClaro,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.cfg_backup_desc),
                    color = Cores.TextoCinza,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { exportarBackup.launch("seguraturbo-backup.json") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            stringResource(R.string.cfg_backup_exportar),
                            color = Cores.Ciano,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(
                        onClick = { importarBackup.launch(arrayOf("*/*")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            stringResource(R.string.cfg_backup_importar),
                            color = Cores.Ciano,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onFechar) {
                Text(stringResource(R.string.btn_ok), color = Cores.Ciano, fontWeight = FontWeight.Bold)
            }
        }
    )
}
