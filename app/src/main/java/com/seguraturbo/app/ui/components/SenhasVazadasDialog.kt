package com.seguraturbo.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seguraturbo.app.R
import com.seguraturbo.app.ui.theme.Cores
import com.seguraturbo.app.util.VerificadorSenhasVazadas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SenhasVazadasDialog(onDismiss: () -> Unit) {
    val contexto = LocalContext.current
    val verificador = remember { VerificadorSenhasVazadas(contexto) }
    val scope = rememberCoroutineScope()
    var senha by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<VerificadorSenhasVazadas.Resultado?>(null) }
    var verificando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Cores.Superficie,
        title = {
            Text(
                stringResource(R.string.senhas_titulo),
                color = Cores.TextoClaro,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    stringResource(R.string.senhas_desc),
                    color = Cores.TextoCinza,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = senha,
                    onValueChange = {
                        senha = it
                        resultado = null
                    },
                    label = { Text(stringResource(R.string.senhas_campo), color = Cores.TextoCinza) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                val res = resultado
                if (res != null) {
                    Text(
                        text = if (res.Encontrado) {
                            stringResource(R.string.senhas_resultado_vazada, res.Contagem)
                        } else {
                            stringResource(R.string.senhas_resultado_segura)
                        },
                        color = if (res.Encontrado) Cores.Amarelo else Cores.Verde,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (senha.isNotBlank()) {
                        verificando = true
                        scope.launch {
                            // Ler o dicionário e calcular o hash é trabalho de fundo.
                            val res = withContext(Dispatchers.IO) {
                                val hash = VerificadorSenhasVazadas.sha1(senha)
                                verificador.verificar(hash)
                            }
                            resultado = res
                            verificando = false
                        }
                    }
                },
                enabled = senha.isNotBlank() && !verificando
            ) {
                Text(stringResource(R.string.senhas_verificar), color = Cores.Ciano)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.senhas_fechar), color = Cores.TextoCinza)
            }
        }
    )
}
