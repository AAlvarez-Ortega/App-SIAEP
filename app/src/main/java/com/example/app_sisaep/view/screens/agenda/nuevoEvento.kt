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
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.app_sisaep.R
import com.example.app_sisaep.model.dto.EventoInsertDto
import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import com.example.app_sisaep.viewModel.consultaas.insertarEventoUsuario
import io.github.jan.supabase.gotrue.auth

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NuevoEventoContent(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val locale = Locale.getDefault()
    val dateText = remember(selectedDate) {
        val fmt = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy", locale)
        selectedDate.format(fmt).replaceFirstChar { it.uppercase() }
    }

    // ---- Form state ----
    var titulo by remember { mutableStateOf("") }




    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }

    // Recursos de Texto
    val errorTitleRequired = stringResource(R.string.error_title_required)
    val errorStartTimeRequired = stringResource(R.string.error_start_time_required)
    val errorInvalidTimeFormat = stringResource(R.string.error_invalid_time_format)
    val errorEndTimeRequired = stringResource(R.string.error_end_time_required)
    val errorEndTimeAfterStart = stringResource(R.string.error_end_time_after_start)
    val errorPlaceRequired = stringResource(R.string.error_place_required)
    val errorCheckMarkedFields = stringResource(R.string.error_check_marked_fields)
    val eventSavedSuccess = "Evento guardado exitosamente."
    val eventSavedError = "Error de red al guardar el evento."

    // ---- Errors ----
    var tituloError by remember { mutableStateOf<String?>(null) }
    var horaInicioError by remember { mutableStateOf<String?>(null) }
    var horaFinError by remember { mutableStateOf<String?>(null) }
    var lugarError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // ---- Bring-into-view requesters ----
    val birTitulo = remember { BringIntoViewRequester() }
    val birHoraInicio = remember { BringIntoViewRequester() }
    val birHoraFin = remember { BringIntoViewRequester() }
    val birLugar = remember { BringIntoViewRequester() }

    //FUNCIÓN DE AUTO-FORMATEO UX Inteligente
    fun formatUserTimeInput(input: String): String {
        val clean = input.trim().replace(" ", "")
        if (clean.isEmpty()) return ""

        // Si el usuario escribe solo un número (ej: "9" o "18")
        if (clean.all { it.isDigit() }) {
            val num = clean.toIntOrNull() ?: return clean
            return if (num in 0..23) String.format(Locale.US, "%02d:00", num) else clean
        }

        // Si escribe algo como "9:3" -> "09:30"
        if (clean.contains(":") && clean.length <= 4) {
            val partes = clean.split(":")
            if (partes.size == 2) {
                val hora = partes[0].toIntOrNull() ?: return clean
                val min = partes[1].toIntOrNull() ?: 0
                if (hora in 0..23 && min in 0..59) {
                    return String.format(Locale.US, "%02d:%02d", hora, min)
                }
            }
        }
        return clean
    }

    // Obtiene sufijo dinámico AM / PM interactivo para la UI
    fun getAmPmSuffix(timeStr: String): String {
        return try {
            val limpiado = timeStr.trim()
            val formato = if (limpiado.contains(":") && limpiado.indexOf(":") == 1) "H:mm" else "HH:mm"
            val localTime = LocalTime.parse(limpiado, DateTimeFormatter.ofPattern(formato))
            localTime.format(DateTimeFormatter.ofPattern("a", Locale.US))
        } catch (_: Exception) {
            ""
        }
    }

    fun parseTimeOrNull(value: String): LocalTime? {
        return try {
            val limpiado = value.trim()
            val formato = if (limpiado.contains(":") && limpiado.indexOf(":") == 1) "H:mm" else "HH:mm"
            LocalTime.parse(limpiado, DateTimeFormatter.ofPattern(formato))
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

        return tituloError == null && horaInicioError == null && horaFinError == null && lugarError == null
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
            .verticalScroll(scrollState)
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.new_event),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = dateText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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
            enabled = !isSaving,
            isError = tituloError != null,
            supportingText = { if (tituloError != null) Text(tituloError!!) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ---- HORA INICIO ----
            OutlinedTextField(
                value = horaInicio,
                onValueChange = {
                    // Permitimos solo números y dos puntos en tiempo real
                    if (it.length <= 5 && it.all { c -> c.isDigit() || c == ':' }) {
                        horaInicio = it
                        if (horaInicioError != null) horaInicioError = null
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .bringIntoViewRequester(birHoraInicio)
                    .onFocusChanged { focusState ->
                        // 🚀 Cuando el usuario cambia de campo, auto-formateamos la entrada
                        if (!focusState.isFocused && horaInicio.isNotEmpty()) {
                            horaInicio = formatUserTimeInput(horaInicio)
                        }
                    }
                    .onFocusEvent { s ->
                        if (s.isFocused) scope.launch {
                            delay(120); birHoraInicio.bringIntoView()
                        }
                    },
                label = { Text(stringResource(R.string.start_time)) },
                placeholder = { Text("09:00") },
                trailingIcon = {
                    val suffix = getAmPmSuffix(horaInicio)
                    if (suffix.isNotEmpty()) {
                        Text(
                            text = suffix,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                },
                singleLine = true,
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = horaInicioError != null,
                supportingText = { if (horaInicioError != null) Text(horaInicioError!!) }
            )

            // ---- HORA FIN ----
            OutlinedTextField(
                value = horaFin,
                onValueChange = {
                    if (it.length <= 5 && it.all { c -> c.isDigit() || c == ':' }) {
                        horaFin = it
                        if (horaFinError != null) horaFinError = null
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .bringIntoViewRequester(birHoraFin)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused && horaFin.isNotEmpty()) {
                            horaFin = formatUserTimeInput(horaFin)
                        }
                    }
                    .onFocusEvent { s ->
                        if (s.isFocused) scope.launch {
                            delay(120); birHoraFin.bringIntoView()
                        }
                    },
                label = { Text(stringResource(R.string.end_time)) },
                placeholder = { Text("10:30") },
                trailingIcon = {
                    val suffix = getAmPmSuffix(horaFin)
                    if (suffix.isNotEmpty()) {
                        Text(
                            text = suffix,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                },
                singleLine = true,
                enabled = !isSaving,
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
            enabled = !isSaving,
            isError = lugarError != null,
            supportingText = { if (lugarError != null) Text(lugarError!!) }
        )

        OutlinedTextField(
            value = notas,
            onValueChange = { notas = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 110.dp),
            label = { Text(stringResource(R.string.notes)) },
            placeholder = { Text(stringResource(R.string.event_details_placeholder)) },
            enabled = !isSaving,
            maxLines = 5
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    focusManager.clearFocus()

                    // Antes de validar, nos aseguramos de dar formato final por si acaso
                    horaInicio = formatUserTimeInput(horaInicio)
                    horaFin = formatUserTimeInput(horaFin)

                    if (!validate()) {
                        scope.launch {
                            snackbarHostState.showSnackbar(errorCheckMarkedFields)
                            bringFirstErrorIntoView()
                        }
                        return@Button
                    }

                    scope.launch {
                        isSaving = true

                        val tIni = parseTimeOrNull(horaInicio)!!
                        val tFin = parseTimeOrNull(horaFin)!!

                        val fEstricta = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

                        val zoneOffset = ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())
                        val timestampInicio = OffsetDateTime.of(selectedDate, tIni, zoneOffset).toString()
                        val timestampFin = OffsetDateTime.of(selectedDate, tFin, zoneOffset).toString()

                        val usuarioIdActual = try {
                            SupabaseConnectionApp.client.auth.currentUserOrNull()?.id
                        } catch (_: Exception) {
                            null
                        }

                        val nuevoEvento = EventoInsertDto(
                            idUsuario = usuarioIdActual,
                            fechaInicio = fEstricta,
                            fechaFin = fEstricta,
                            horaInicio = timestampInicio,
                            horaFin = timestampFin,
                            titulo = "$titulo",
                            lugar = lugar.trim().ifEmpty { null },
                            notas = notas.trim().ifEmpty { null }
                        )

                        val seGuardo = insertarEventoUsuario(nuevoEvento)

                        if (seGuardo) {
                            snackbarHostState.showSnackbar(eventSavedSuccess)
                            titulo = ""
                            horaInicio = ""
                            horaFin = ""
                            lugar = ""
                            notas = ""
                            onSaveSuccess()
                        } else {
                            snackbarHostState.showSnackbar(eventSavedError)
                        }
                        isSaving = false
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.weight(1f)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.save))
                }
            }
        }

        Spacer(modifier = Modifier.height(90.dp))
        SnackbarHost(hostState = snackbarHostState)
    }
}