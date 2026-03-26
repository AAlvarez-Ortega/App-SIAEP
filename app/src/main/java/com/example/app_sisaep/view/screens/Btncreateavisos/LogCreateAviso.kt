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
import androidx.compose.ui.res.stringResource
import com.example.app_sisaep.R

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
        title = { Text(stringResource(R.string.new_global_notice)) },
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
                    label = { Text(stringResource(R.string.title_required)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = mensaje,
                    onValueChange = { mensaje = it },
                    label = { Text(stringResource(R.string.message_required)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Text(stringResource(R.string.category_label), style = MaterialTheme.typography.labelMedium)
                val tipos = listOf(
                    "Informativo" to R.string.tipo_informativo,
                    "Urgente" to R.string.tipo_urgente,
                    "Evento" to R.string.tipo_evento
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    tipos.forEach { (value, labelRes) ->
                        FilterChip(
                            selected = tipoAviso == value,
                            onClick = { tipoAviso = value },
                            label = {
                                Text(
                                    text = stringResource(labelRes),
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }

                HorizontalDivider()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = tieneExpiracion, onCheckedChange = { tieneExpiracion = it })
                    Text(stringResource(R.string.set_expiration))
                }

                AnimatedVisibility(visible = tieneExpiracion) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Text(
                                if (datePickerState.selectedDateMillis == null)
                                    stringResource(R.string.select_date)
                                else
                                    stringResource(R.string.change_date)
                            )
                        }

                        Button(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Text(
                                "${stringResource(R.string.configure_time)}: ${
                                    "%02d".format(timePickerState.hour)
                                }:${"%02d".format(timePickerState.minute)}"
                            )
                        }
                    }
                }

                if (!tieneExpiracion) {
                    Text(
                        stringResource(R.string.auto_delete_note),
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
                            Toast.makeText(
                                context,
                                context.getString(R.string.date_must_be_future),
                                Toast.LENGTH_SHORT
                            ).show()
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
                            Toast.makeText(
                                context,
                                context.getString(R.string.notice_success),
                                Toast.LENGTH_LONG
                            ).show()
                            onSuccess()
                            onDismiss()
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.server_error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                enabled = titulo.isNotBlank() && mensaje.isNotBlank()
            ) {
                Text(stringResource(R.string.publish_button))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_button)) } }
    )

    // Modales de selección
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.ok_button)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.ok_button)) }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}