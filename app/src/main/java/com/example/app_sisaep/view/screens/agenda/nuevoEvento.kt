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

import androidx.compose.ui.res.stringResource
import com.example.app_sisaep.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NuevoEventoContent(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
    onSaveSimulated: () -> Unit = {}
) {
    val locale = Locale.getDefault()
    val dateText = remember(selectedDate) {
        val fmt = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy", locale)
        selectedDate.format(fmt).replaceFirstChar { it.uppercase() }
    }

    // ---- Form state (simulado) ----
    var titulo by remember { mutableStateOf("") }

    val classLabel = stringResource(R.string.event_type_class)
    val tutoringLabel = stringResource(R.string.event_type_tutoring)
    val reminderLabel = stringResource(R.string.event_type_reminder)

    var tipo by remember { mutableStateOf(classLabel) }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    val errorTitleRequired = stringResource(R.string.error_title_required)
    val errorStartTimeRequired = stringResource(R.string.error_start_time_required)
    val errorInvalidTimeFormat = stringResource(R.string.error_invalid_time_format)
    val errorEndTimeRequired = stringResource(R.string.error_end_time_required)
    val errorEndTimeAfterStart = stringResource(R.string.error_end_time_after_start)
    val errorPlaceRequired = stringResource(R.string.error_place_required)
    val errorCheckMarkedFields = stringResource(R.string.error_check_marked_fields)
    val eventSavedSimulated = stringResource(R.string.event_saved_simulated)

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

        if (titulo.trim().isEmpty()) tituloError = errorTitleRequired

        val tIni = parseTimeOrNull(horaInicio)
        val tFin = parseTimeOrNull(horaFin)

        if (horaInicio.trim().isEmpty()) {
            horaInicioError = errorStartTimeRequired
        } else if (tIni == null) {
            horaInicioError = errorInvalidTimeFormat
        }

        if (horaFin.trim().isEmpty()) {
            horaFinError = errorEndTimeRequired
        } else if (tFin == null) {
            horaFinError = errorInvalidTimeFormat
        }

        if (tIni != null && tFin != null && !tFin.isAfter(tIni)) {
            horaFinError = errorEndTimeAfterStart
        }

        if (lugar.trim().isEmpty()) lugarError = errorPlaceRequired

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
            text = stringResource(R.string.new_event),
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = dateText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = tipo == classLabel,
                onClick = { tipo = classLabel },
                label = { Text(classLabel) }
            )
            FilterChip(
                selected = tipo == tutoringLabel,
                onClick = { tipo = tutoringLabel },
                label = { Text(tutoringLabel) }
            )
            FilterChip(
                selected = tipo == reminderLabel,
                onClick = { tipo = reminderLabel },
                label = { Text(reminderLabel) }
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
            label = { Text(stringResource(R.string.title)) },
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
                label = { Text(stringResource(R.string.start_time)) },
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
                label = { Text(stringResource(R.string.end_time)) },
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
            label = { Text(stringResource(R.string.place)) },
            placeholder = { Text(stringResource(R.string.place_placeholder)) },
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
            label = { Text(stringResource(R.string.notes)) },
            placeholder = { Text(stringResource(R.string.event_details_placeholder)) },
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
                            snackbarHostState.showSnackbar(errorCheckMarkedFields)
                            bringFirstErrorIntoView()
                        }
                        return@Button
                    }

                    onSaveSimulated()
                    scope.launch { snackbarHostState.showSnackbar(eventSavedSimulated)}

                    titulo = ""
                    horaInicio = ""
                    horaFin = ""
                    lugar = ""
                    notas = ""
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.save))
            }
        }

        // ✅ espacio para que los FAB no tapen el final
        Spacer(modifier = Modifier.height(90.dp))

        SnackbarHost(hostState = snackbarHostState)
    }
}
