package com.seguraturbo.app.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seguraturbo.app.ui.theme.Cores
import com.seguraturbo.app.util.VerificadorSenhasVazadas

@Composable
fun SenhasVazadasDialog(onDismiss: () -> Unit) {
    val contexto = LocalContext.current
    val verificador = remember { VerificadorSenhasVazadas(contexto) }
    var senha by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<VerificadorSenhasVazadas.Resultado?>(null) }
    var verificando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Cores.Superficie,
        title = {
            Text(
                "Verificador de senhas vazadas (offline)",
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
                    "Tudo local. A senha não sai do aparelho. Usa SHA-1 + k-anonymity (apenas prefixo consultado no dicionário).",
                    color = Cores.TextoCinza,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = senha,
                    onValueChange = { 
                        senha = it
                        resultado = null
                    },
                    label = { Text("Senha", color = Cores.TextoCinza) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (resultado != null) {
                    val res = resultado!!
                    Text(
                        text = if (res.Encontrado) "⚠ ${res.Mensagem}" else "✓ ${res.Mensagem}",
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
                        val hash = verificador.sha1(senha)
                        resultado = verificador.verificar(hash)
                        verificando = false
                    }
                },
                enabled = senha.isNotBlank() && !verificando
            ) {
                Text("Verificar", color = Cores.Ciano)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = Cores.TextoCinza)
            }
        }
    )
}
