package com.seguraturbo.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seguraturbo.app.ui.theme.Cores

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoraDialog(
    titulo: String,
    horaInicial: Int,
    minutoInicial: Int,
    onConfirmar: (hora: Int, minuto: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val estado = rememberTimePickerState(
        initialHour = horaInicial.coerceIn(0, 23),
        initialMinute = minutoInicial.coerceIn(0, 59),
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Cores.Superficie,
        title = { Text(titulo, color = Cores.TextoClaro, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimePicker(state = estado)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(estado.hour, estado.minute) }) {
                Text("OK", color = Cores.Ciano, fontSize = 14.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Cores.TextoCinza, fontSize = 14.sp)
            }
        }
    )
}
