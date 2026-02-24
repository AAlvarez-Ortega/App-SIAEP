package com.example.app_sisaep.view.screens.agenda

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.relocation.bringIntoViewRequester
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
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

    // ---- Form state (simulado) ----
    var titulo by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Clase") }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    // ---- Errors ----
    var tituloError by remember { mutableStateOf<String?>(null) }
    var horaInicioError by remember { mutableStateOf<String?>(null) }
    var horaFinError by remember { mutableStateOf<String?>(null) }
    var lugarError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // ---- Scroll ----
    val scrollState = rememberScrollState()

    // ---- Bring-into-view requesters ----
    val birTitulo = remember { BringIntoViewRequester() }
    val birHoraInicio = remember { BringIntoViewRequester() }
    val birHoraFin = remember { BringIntoViewRequester() }
    val birLugar = remember { BringIntoViewRequester() }
    val birNotas = remember { BringIntoViewRequester() }

    fun parseTimeOrNull(value: String): LocalTime? {
        return try {
            LocalTime.parse(value.trim(), DateTimeFormatter.ofPattern("H:mm"))
        } catch (_: DateTimeParseException) {
            null
        }
    }

    fun validate(): Boolean {
        tituloError = null
        horaInicioError = null
        horaFinError = null
        lugarError = null

        if (titulo.trim().isEmpty()) tituloError = "El título es obligatorio."

        val tIni = parseTimeOrNull(horaInicio)
        val tFin = parseTimeOrNull(horaFin)

        if (horaInicio.trim().isEmpty()) {
            horaInicioError = "Hora inicio obligatoria (HH:mm)."
        } else if (tIni == null) {
            horaInicioError = "Formato inválido. Usa HH:mm (ej. 09:00)."
        }

        if (horaFin.trim().isEmpty()) {
            horaFinError = "Hora fin obligatoria (HH:mm)."
        } else if (tFin == null) {
            horaFinError = "Formato inválido. Usa HH:mm (ej. 10:30)."
        }

        if (tIni != null && tFin != null && !tFin.isAfter(tIni)) {
            horaFinError = "La hora fin debe ser mayor a la hora inicio."
        }

        if (lugar.trim().isEmpty()) lugarError = "El lugar es obligatorio."

        return tituloError == null &&
                horaInicioError == null &&
                horaFinError == null &&
                lugarError == null
    }

    suspend fun bringFirstErrorIntoView() {
        delay(60)
        when {
            tituloError != null -> birTitulo.bringIntoView()
            horaInicioError != null -> birHoraInicio.bringIntoView()
            horaFinError != null -> birHoraFin.bringIntoView()
            lugarError != null -> birLugar.bringIntoView()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)  // ✅ scroll
            .imePadding()                 // ✅ teclado no tapa
            .navigationBarsPadding()      // ✅ respeta navbar
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Nuevo evento",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = dateText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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
            onValueChange = {
                titulo = it
                if (tituloError != null) tituloError = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(birTitulo)
                .onFocusEvent { s ->
                    if (s.isFocused) scope.launch {
                        delay(120); birTitulo.bringIntoView()
                    }
                },
            label = { Text("Título") },
            singleLine = true,
            isError = tituloError != null,
            supportingText = { if (tituloError != null) Text(tituloError!!) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = horaInicio,
                onValueChange = {
                    horaInicio = it
                    if (horaInicioError != null) horaInicioError = null
                },
                modifier = Modifier
                    .weight(1f)
                    .bringIntoViewRequester(birHoraInicio)
                    .onFocusEvent { s ->
                        if (s.isFocused) scope.launch {
                            delay(120); birHoraInicio.bringIntoView()
                        }
                    },
                label = { Text("Hora inicio") },
                placeholder = { Text("09:00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = horaInicioError != null,
                supportingText = { if (horaInicioError != null) Text(horaInicioError!!) }
            )

            OutlinedTextField(
                value = horaFin,
                onValueChange = {
                    horaFin = it
                    if (horaFinError != null) horaFinError = null
                },
                modifier = Modifier
                    .weight(1f)
                    .bringIntoViewRequester(birHoraFin)
                    .onFocusEvent { s ->
                        if (s.isFocused) scope.launch {
                            delay(120); birHoraFin.bringIntoView()
                        }
                    },
                label = { Text("Hora fin") },
                placeholder = { Text("10:30") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = horaFinError != null,
                supportingText = { if (horaFinError != null) Text(horaFinError!!) }
            )
        }

        OutlinedTextField(
            value = lugar,
            onValueChange = {
                lugar = it
                if (lugarError != null) lugarError = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(birLugar)
                .onFocusEvent { s ->
                    if (s.isFocused) scope.launch {
                        delay(120); birLugar.bringIntoView()
                    }
                },
            label = { Text("Lugar") },
            placeholder = { Text("Aula 203 / Laboratorio / Online") },
            singleLine = true,
            isError = lugarError != null,
            supportingText = { if (lugarError != null) Text(lugarError!!) }
        )

        OutlinedTextField(
            value = notas,
            onValueChange = { notas = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 110.dp)
                .bringIntoViewRequester(birNotas)
                .onFocusEvent { s ->
                    if (s.isFocused) scope.launch {
                        delay(120); birNotas.bringIntoView()
                    }
                },
            label = { Text("Notas") },
            placeholder = { Text("Detalles del evento…") },
            maxLines = 5
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Button(
                onClick = {
                    focusManager.clearFocus()

                    val ok = validate()
                    if (!ok) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Revisa los campos marcados ⚠️")
                            bringFirstErrorIntoView()
                        }
                        return@Button
                    }

                    onSaveSimulated()
                    scope.launch { snackbarHostState.showSnackbar("Evento guardado (simulado) ✅") }

                    titulo = ""
                    horaInicio = ""
                    horaFin = ""
                    lugar = ""
                    notas = ""
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar")
            }
        }

        // ✅ espacio para que los FAB no tapen el final
        Spacer(modifier = Modifier.height(90.dp))

        SnackbarHost(hostState = snackbarHostState)
    }
}
