package com.example.app_sisaep.view.screens.Btncreateavisos

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.app_sisaep.viewModel.crearAviso
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogCreateAviso(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Estados del formulario
    var titulo by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var tipoAviso by remember { mutableStateOf("General") }
    var tieneExpiracion by remember { mutableStateOf(false) }

    // Estados para Date y Time Picker
    val calendar = Calendar.getInstance()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE)
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Formateador para mostrar la selección
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Aviso Global") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { if (it.length <= 50) titulo = it },
                    label = { Text("Título (Obligatorio)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = mensaje,
                    onValueChange = { mensaje = it },
                    label = { Text("Mensaje (Obligatorio)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Text("Categoría:", style = MaterialTheme.typography.labelMedium)
                val tipos = listOf("Informativo", "Urgente", "Evento")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    tipos.forEach { tipo ->
                        FilterChip(
                            selected = tipoAviso == tipo,
                            onClick = { tipoAviso = tipo },
                            label = { Text(tipo) }
                        )
                    }
                }

                HorizontalDivider()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = tieneExpiracion, onCheckedChange = { tieneExpiracion = it })
                    Text("Establecer fecha de expiración")
                }

                AnimatedVisibility(visible = tieneExpiracion) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Text(if (datePickerState.selectedDateMillis == null) "Seleccionar Fecha" else "Cambiar Fecha")
                        }

                        Button(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Text("Configurar Hora: ${"%02d".format(timePickerState.hour)}:${"%02d".format(timePickerState.minute)}")
                        }
                    }
                }

                if (!tieneExpiracion) {
                    Text(
                        "Nota: Se eliminará automáticamente en 24 horas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ahora = LocalDateTime.now()
                    var fechaFinalStr: String

                    if (tieneExpiracion && datePickerState.selectedDateMillis != null) {
                        val fechaSeleccionada = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(datePickerState.selectedDateMillis!!),
                            ZoneId.systemDefault()
                        ).withHour(timePickerState.hour).withMinute(timePickerState.minute)

                        // Validación de seguridad: No fechas pasadas
                        if (fechaSeleccionada.isBefore(ahora)) {
                            Toast.makeText(context, "La fecha debe ser futura", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        fechaFinalStr = fechaSeleccionada.format(formatter)
                    } else {
                        // Lógica automática de 24 horas
                        fechaFinalStr = ahora.plusHours(24).format(formatter)
                    }

                    scope.launch {
                        val exito = crearAviso(titulo, mensaje, tipoAviso, fechaFinalStr)
                        if (exito) {
                            Toast.makeText(context, "Aviso publicado correctamente", Toast.LENGTH_LONG).show()
                            onSuccess()
                            onDismiss()
                        } else {
                            Toast.makeText(context, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = titulo.isNotBlank() && mensaje.isNotBlank()
            ) {
                Text("Publicar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    // Modales de selección
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("OK") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}