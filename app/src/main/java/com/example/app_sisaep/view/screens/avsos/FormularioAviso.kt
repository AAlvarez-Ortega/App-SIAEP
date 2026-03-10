package com.example.app_sisaep.view.screens.avsos

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import java.util.Calendar
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormularioAviso(
    onCancelar: () -> Unit,
    onPublicar: (String, String, String, String?) -> Unit
) {

    val guindaIPN = Color(0xFF6A1B2E)

    var titulo by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("informativo") }

    var usarExpiracion by remember { mutableStateOf(false) }

    var fechaExpiracion by remember { mutableStateOf<LocalDate?>(null) }
    var horaExpiracion by remember { mutableStateOf<LocalTime?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val tipos = listOf(
        "informativo",
        "urgente",
        "evento",
        "mantenimiento"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                Text(
                    text = "Nuevo aviso",
                    style = MaterialTheme.typography.headlineSmall,
                    color = guindaIPN
                )

                Divider()

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = mensaje,
                    onValueChange = { mensaje = it },
                    label = { Text("Mensaje") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                Text(
                    text = "Tipo de aviso",
                    style = MaterialTheme.typography.titleMedium,
                    color = guindaIPN
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    tipos.forEach {

                        FilterChip(
                            selected = tipo == it,
                            onClick = { tipo = it },
                            label = {
                                Text(
                                    it.replaceFirstChar { c -> c.uppercase() }
                                )
                            }
                        )

                    }

                }

                Divider()

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Checkbox(
                        checked = usarExpiracion,
                        onCheckedChange = { usarExpiracion = it }
                    )

                    Text("Elegir fecha de expiración")

                }

                if (usarExpiracion) {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        DatePickerDialogExample {
                            fechaExpiracion = it
                        }

                        OutlinedTextField(
                            value = fechaExpiracion?.toString() ?: "",
                            onValueChange = {},
                            label = { Text("Fecha") },
                            readOnly = true,
                            modifier = Modifier.width(150.dp)
                        )

                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        TimePickerDialogExample {
                            horaExpiracion = it
                        }

                        OutlinedTextField(
                            value = horaExpiracion?.toString() ?: "",
                            onValueChange = {},
                            label = { Text("Hora") },
                            readOnly = true,
                            modifier = Modifier.width(150.dp)
                        )

                    }

                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    OutlinedButton(
                        onClick = onCancelar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {

                            val fechaFinal = if (usarExpiracion) {

                                if (fechaExpiracion != null && horaExpiracion != null) {
                                    "${fechaExpiracion}T${horaExpiracion}"
                                } else null

                            } else {

                                val expiracion =
                                    kotlinx.datetime.Clock.System.now() + 24.hours

                                expiracion.toString()

                            }

                            onPublicar(
                                titulo,
                                mensaje,
                                tipo,
                                fechaFinal
                            )

                            scope.launch {
                                snackbarHostState.showSnackbar("Aviso publicado")
                            }

                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = guindaIPN
                        )
                    ) {
                        Text("Publicar")
                    }

                }

            }

        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

    }

}
@Composable
fun DatePickerDialogExample(
    onDateSelected: (LocalDate) -> Unit
) {

    val context = LocalContext.current

    Button(onClick = {

        val calendario = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, day ->

                onDateSelected(
                    LocalDate(year, month + 1, day)
                )

            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)

        ).show()

    }) {

        Text("Seleccionar fecha")

    }

}


@Composable
fun TimePickerDialogExample(
    onTimeSelected: (LocalTime) -> Unit
) {

    val context = LocalContext.current

    Button(onClick = {

        val calendario = Calendar.getInstance()

        TimePickerDialog(
            context,
            { _, hour, minute ->

                onTimeSelected(
                    LocalTime(hour, minute)
                )

            },
            calendario.get(Calendar.HOUR_OF_DAY),
            calendario.get(Calendar.MINUTE),
            true

        ).show()

    }) {

        Text("Seleccionar hora")

    }

}

