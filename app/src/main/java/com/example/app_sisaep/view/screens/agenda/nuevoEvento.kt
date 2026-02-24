package com.example.app_sisaep.view.screens.agenda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NuevoEventoContent(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
    onSaveSimulated: () -> Unit = {}
) {
    val locale = remember { Locale("es", "MX") }
    val dateText = remember(selectedDate) {
        val fmt = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy", locale)
        selectedDate.format(fmt).replaceFirstChar { it.uppercase() }
    }

    // Estado del formulario (simulado)
    var titulo by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Clase") }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())   // 🔥 ESTA ES LA CLAVE
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    )
 {
        Text(
            text = "Nuevo evento",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = dateText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Tipo (chips)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = tipo == "Clase",
                onClick = { tipo = "Clase" },
                label = { Text("Clase") }
            )
            FilterChip(
                selected = tipo == "Asesoría",
                onClick = { tipo = "Asesoría" },
                label = { Text("Asesoría") }
            )
            FilterChip(
                selected = tipo == "Recordatorio",
                onClick = { tipo = "Recordatorio" },
                label = { Text("Recordatorio") }
            )
        }

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Título") },
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = horaInicio,
                onValueChange = { horaInicio = it },
                modifier = Modifier.weight(1f),
                label = { Text("Hora inicio") },
                placeholder = { Text("09:00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = horaFin,
                onValueChange = { horaFin = it },
                modifier = Modifier.weight(1f),
                label = { Text("Hora fin") },
                placeholder = { Text("10:30") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        OutlinedTextField(
            value = lugar,
            onValueChange = { lugar = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Lugar") },
            placeholder = { Text("Aula 203 / Laboratorio / Online") },
            singleLine = true
        )

        OutlinedTextField(
            value = notas,
            onValueChange = { notas = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 110.dp),
            label = { Text("Notas") },
            placeholder = { Text("Detalles del evento…") },
            maxLines = 5
        )

        Spacer(Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    onSaveSimulated()

                    // ✅ Snackbar correcto (sin LaunchedEffect)
                    scope.launch {
                        snackbarHostState.showSnackbar("Evento guardado (simulado) ✅")
                    }

                    // Limpieza opcional
                    titulo = ""
                    horaInicio = ""
                    horaFin = ""
                    lugar = ""
                    notas = ""
                },
                modifier = Modifier.weight(1f),
                enabled = titulo.isNotBlank()
            ) {
                Text("Guardar")
            }
            Spacer(modifier = Modifier.height(90.dp))

        }

        SnackbarHost(hostState = snackbarHostState)
    }


}

