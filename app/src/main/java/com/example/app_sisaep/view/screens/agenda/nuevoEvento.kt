package com.example.app_sisaep.view.screens.agenda

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_sisaep.R
import com.example.app_sisaep.model.dto.EventoInsertDto
import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import com.example.app_sisaep.viewModel.consultaas.insertarEventoUsuario
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

// ── Paleta institucional ───────────────────────────────────────────────────────
private val GuindaPrimario  = Color(0xFF8A1F4D)
private val GuindaOscuro    = Color(0xFF6B1840)
private val GuindaSuave     = Color(0xFFF5E6ED)
private val TextoSecundario = Color(0xFF6B7280)

// ── Utilidades de tiempo (funciones puras) ────────────────────────────────────
private fun formatUserTimeInput(input: String): String {
    val clean = input.trim().replace(" ", "")
    if (clean.isEmpty()) return ""
    if (clean.all { it.isDigit() }) {
        val num = clean.toIntOrNull() ?: return clean
        return if (num in 0..23) String.format(Locale.US, "%02d:00", num) else clean
    }
    if (clean.contains(":") && clean.length <= 4) {
        val partes = clean.split(":")
        if (partes.size == 2) {
            val hora = partes[0].toIntOrNull() ?: return clean
            val min  = partes[1].toIntOrNull() ?: 0
            if (hora in 0..23 && min in 0..59) {
                return String.format(Locale.US, "%02d:%02d", hora, min)
            }
        }
    }
    return clean
}

private fun getAmPmSuffix(timeStr: String): String {
    return try {
        val limpiado = timeStr.trim()
        val formato  = if (limpiado.contains(":") && limpiado.indexOf(":") == 1) "H:mm" else "HH:mm"
        LocalTime.parse(limpiado, DateTimeFormatter.ofPattern(formato))
            .format(DateTimeFormatter.ofPattern("a", Locale.US))
    } catch (_: Exception) { "" }
}

private fun parseTimeOrNull(value: String): LocalTime? {
    return try {
        val limpiado = value.trim()
        val formato  = if (limpiado.contains(":") && limpiado.indexOf(":") == 1) "H:mm" else "HH:mm"
        LocalTime.parse(limpiado, DateTimeFormatter.ofPattern(formato))
    } catch (_: DateTimeParseException) { null }
}

// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NuevoEventoContent(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val locale   = Locale.getDefault()
    val dateText = remember(selectedDate) {
        val fmt = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy", locale)
        selectedDate.format(fmt).replaceFirstChar { it.uppercase() }
    }

    // Form state
    var titulo     by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin    by remember { mutableStateOf("") }
    var lugar      by remember { mutableStateOf("") }
    var notas      by remember { mutableStateOf("") }
    var isSaving   by remember { mutableStateOf(false) }

    // Strings de recursos
    val errorTitleRequired    = stringResource(R.string.error_title_required)
    val errorStartTimeRequired = stringResource(R.string.error_start_time_required)
    val errorInvalidTimeFormat = stringResource(R.string.error_invalid_time_format)
    val errorEndTimeRequired   = stringResource(R.string.error_end_time_required)
    val errorEndTimeAfterStart = stringResource(R.string.error_end_time_after_start)
    val errorPlaceRequired     = stringResource(R.string.error_place_required)
    val errorCheckMarkedFields = stringResource(R.string.error_check_marked_fields)

    // Errores
    var tituloError     by remember { mutableStateOf<String?>(null) }
    var horaInicioError by remember { mutableStateOf<String?>(null) }
    var horaFinError    by remember { mutableStateOf<String?>(null) }
    var lugarError      by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()
    val focusManager      = LocalFocusManager.current
    val scrollState       = rememberScrollState()

    // Bring-into-view requesters
    val birTitulo     = remember { BringIntoViewRequester() }
    val birHoraInicio = remember { BringIntoViewRequester() }
    val birHoraFin    = remember { BringIntoViewRequester() }
    val birLugar      = remember { BringIntoViewRequester() }

    fun validate(): Boolean {
        tituloError     = null
        horaInicioError = null
        horaFinError    = null
        lugarError      = null

        if (titulo.trim().isEmpty()) tituloError = errorTitleRequired

        val tIni = parseTimeOrNull(horaInicio)
        val tFin = parseTimeOrNull(horaFin)

        when {
            horaInicio.trim().isEmpty() -> horaInicioError = errorStartTimeRequired
            tIni == null               -> horaInicioError = errorInvalidTimeFormat
        }
        when {
            horaFin.trim().isEmpty() -> horaFinError = errorEndTimeRequired
            tFin == null             -> horaFinError = errorInvalidTimeFormat
        }
        if (tIni != null && tFin != null && !tFin.isAfter(tIni))
            horaFinError = errorEndTimeAfterStart

        if (lugar.trim().isEmpty()) lugarError = errorPlaceRequired

        return tituloError == null && horaInicioError == null &&
                horaFinError == null && lugarError == null
    }

    suspend fun bringFirstErrorIntoView() {
        delay(60)
        when {
            tituloError != null     -> birTitulo.bringIntoView()
            horaInicioError != null -> birHoraInicio.bringIntoView()
            horaFinError != null    -> birHoraFin.bringIntoView()
            lugarError != null      -> birLugar.bringIntoView()
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .navigationBarsPadding()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Cabecera ──────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.new_event),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario
                )
            }

            // ── Sección: Título ───────────────────────────────────────────────
            SeccionFormulario(titulo = "Título del evento", icono = Icons.Rounded.Title) {
                CampoTexto(
                    value = titulo,
                    onValueChange = { titulo = it; if (tituloError != null) tituloError = null },
                    label = stringResource(R.string.title),
                    placeholder = "Ej. Entrega de proyecto final",
                    error = tituloError,
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(birTitulo)
                        .onFocusEvent { s ->
                            if (s.isFocused) scope.launch { delay(120); birTitulo.bringIntoView() }
                        }
                )
            }

            // ── Sección: Horario (firma visual: pastilla conectada) ────────────
            SeccionFormulario(titulo = "Horario", icono = Icons.Rounded.AccessTime) {
                BloquePastillaHorario(
                    horaInicio = horaInicio,
                    horaFin = horaFin,
                    horaInicioError = horaInicioError,
                    horaFinError = horaFinError,
                    enabled = !isSaving,
                    birHoraInicio = birHoraInicio,
                    birHoraFin = birHoraFin,
                    onHoraInicioChange = { v ->
                        if (v.length <= 5 && v.all { c -> c.isDigit() || c == ':' }) {
                            horaInicio = v
                            if (horaInicioError != null) horaInicioError = null
                        }
                    },
                    onHoraFinChange = { v ->
                        if (v.length <= 5 && v.all { c -> c.isDigit() || c == ':' }) {
                            horaFin = v
                            if (horaFinError != null) horaFinError = null
                        }
                    },
                    onHoraInicioBlur = {
                        if (horaInicio.isNotEmpty()) horaInicio = formatUserTimeInput(horaInicio)
                    },
                    onHoraFinBlur = {
                        if (horaFin.isNotEmpty()) horaFin = formatUserTimeInput(horaFin)
                    },
                    scope = scope
                )
            }

            // ── Sección: Lugar ────────────────────────────────────────────────
            SeccionFormulario(titulo = "Ubicación", icono = Icons.Rounded.Place) {
                CampoTexto(
                    value = lugar,
                    onValueChange = { lugar = it; if (lugarError != null) lugarError = null },
                    label = stringResource(R.string.place),
                    placeholder = stringResource(R.string.place_placeholder),
                    error = lugarError,
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(birLugar)
                        .onFocusEvent { s ->
                            if (s.isFocused) scope.launch { delay(120); birLugar.bringIntoView() }
                        }
                )
            }

            // ── Sección: Notas ────────────────────────────────────────────────
            SeccionFormulario(titulo = "Notas (opcional)", icono = Icons.Rounded.EditNote) {
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp),
                    placeholder = { Text(stringResource(R.string.event_details_placeholder), color = TextoSecundario) },
                    enabled = !isSaving,
                    maxLines = 5,
                    shape = RoundedCornerShape(14.dp),
                    colors = campoColores()
                )
            }

            // ── Botones ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Botón cancelar
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isSaving,
                    modifier = Modifier.weight(0.38f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GuindaPrimario),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GuindaPrimario.copy(alpha = 0.5f))
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Medium)
                }

                // Botón guardar (guinda, con indicador inline)
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        horaInicio = formatUserTimeInput(horaInicio)
                        horaFin    = formatUserTimeInput(horaFin)

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
                            val fEstricta    = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            val zoneOffset   = ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())
                            val tsInicio     = OffsetDateTime.of(selectedDate, tIni, zoneOffset).toString()
                            val tsFin        = OffsetDateTime.of(selectedDate, tFin, zoneOffset).toString()
                            val usuarioId    = try {
                                SupabaseConnectionApp.client.auth.currentUserOrNull()?.id
                            } catch (_: Exception) { null }

                            val nuevoEvento = EventoInsertDto(
                                idUsuario   = usuarioId,
                                fechaInicio = fEstricta,
                                fechaFin    = fEstricta,
                                horaInicio  = tsInicio,
                                horaFin     = tsFin,
                                titulo      = titulo.trim(),
                                lugar       = lugar.trim().ifEmpty { null },
                                notas       = notas.trim().ifEmpty { null }
                            )

                            if (insertarEventoUsuario(nuevoEvento)) {
                                snackbarHostState.showSnackbar("Evento guardado ✓")
                                titulo = ""; horaInicio = ""; horaFin = ""
                                lugar = ""; notas = ""
                                onSaveSuccess()
                            } else {
                                snackbarHostState.showSnackbar("Error de red al guardar el evento.")
                            }
                            isSaving = false
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.weight(0.62f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GuindaPrimario)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            stringResource(R.string.save),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ── Componente: Contenedor de sección con icono + título ─────────────────────
@Composable
private fun SeccionFormulario(
    titulo: String,
    icono: ImageVector,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = GuindaPrimario,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = GuindaPrimario
            )
        }
        contenido()
    }
}

// ── Componente: Campo de texto estilizado ─────────────────────────────────────
@Composable
private fun CampoTexto(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    error: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder, color = TextoSecundario) },
            singleLine = singleLine,
            enabled = enabled,
            isError = error != null,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(14.dp),
            colors = campoColores()
        )
        // Error animado
        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn(tween(200)) + expandVertically(tween(200)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
        ) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 14.dp, top = 4.dp)
            )
        }
    }
}

// ── Componente FIRMA: Pastilla visual de horario inicio → fin ─────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BloquePastillaHorario(
    horaInicio: String,
    horaFin: String,
    horaInicioError: String?,
    horaFinError: String?,
    enabled: Boolean,
    birHoraInicio: BringIntoViewRequester,
    birHoraFin: BringIntoViewRequester,
    onHoraInicioChange: (String) -> Unit,
    onHoraFinChange: (String) -> Unit,
    onHoraInicioBlur: () -> Unit,
    onHoraFinBlur: () -> Unit,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val hayError    = horaInicioError != null || horaFinError != null
    val borderColor by animateColorAsState(
        targetValue = if (hayError) MaterialTheme.colorScheme.error
        else GuindaPrimario.copy(alpha = 0.3f),
        label = "PastillaBorder"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GuindaSuave)
                .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Campo Hora Inicio
                CampoHoraPastilla(
                    value = horaInicio,
                    onValueChange = onHoraInicioChange,
                    label = "Inicio",
                    placeholder = "09:00",
                    enabled = enabled,
                    isError = horaInicioError != null,
                    modifier = Modifier
                        .weight(1f)
                        .bringIntoViewRequester(birHoraInicio)
                        .onFocusChanged { if (!it.isFocused) onHoraInicioBlur() }
                        .onFocusEvent { s ->
                            if (s.isFocused) scope.launch { delay(120); birHoraInicio.bringIntoView() }
                        }
                )

                // Flecha central (firma visual)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = GuindaPrimario,
                        modifier = Modifier.size(20.dp)
                    )
                    val duracion = calcularDuracion(horaInicio, horaFin)
                    if (duracion.isNotEmpty()) {
                        Text(
                            text = duracion,
                            style = MaterialTheme.typography.labelSmall,
                            color = GuindaPrimario,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        )
                    }
                }

                // Campo Hora Fin
                CampoHoraPastilla(
                    value = horaFin,
                    onValueChange = onHoraFinChange,
                    label = "Fin",
                    placeholder = "10:30",
                    enabled = enabled,
                    isError = horaFinError != null,
                    modifier = Modifier
                        .weight(1f)
                        .bringIntoViewRequester(birHoraFin)
                        .onFocusChanged { if (!it.isFocused) onHoraFinBlur() }
                        .onFocusEvent { s ->
                            if (s.isFocused) scope.launch { delay(120); birHoraFin.bringIntoView() }
                        }
                )
            }
        }

        // Mensajes de error animados debajo de la pastilla
        AnimatedVisibility(
            visible = horaInicioError != null,
            enter = fadeIn(tween(200)) + expandVertically(tween(200)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
        ) {
            Text(
                text = "Inicio: ${horaInicioError ?: ""}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 14.dp)
            )
        }
        AnimatedVisibility(
            visible = horaFinError != null,
            enter = fadeIn(tween(200)) + expandVertically(tween(200)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
        ) {
            Text(
                text = "Fin: ${horaFinError ?: ""}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 14.dp)
            )
        }
    }
}

// ── Sub-campo minimalista dentro de la pastilla ───────────────────────────────
@Composable
private fun CampoHoraPastilla(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    enabled: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val suffix = getAmPmSuffix(value)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isError) MaterialTheme.colorScheme.error else TextoSecundario,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextoSecundario.copy(alpha = 0.6f), fontSize = 16.sp) },
            singleLine = true,
            enabled = enabled,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = LocalTextStyle.current.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            ),
            suffix = if (suffix.isNotEmpty()) ({
                Text(
                    text = suffix,
                    style = MaterialTheme.typography.labelSmall,
                    color = GuindaPrimario,
                    fontWeight = FontWeight.Bold
                )
            }) else null,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = GuindaPrimario,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor     = MaterialTheme.colorScheme.error,
                focusedContainerColor   = Color.White,
                unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                errorContainerColor     = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            )
        )
    }
}

// ── Calcula duración entre dos horas para mostrar en la pastilla ──────────────
private fun calcularDuracion(inicio: String, fin: String): String {
    val tIni = parseTimeOrNull(inicio) ?: return ""
    val tFin = parseTimeOrNull(fin) ?: return ""
    if (!tFin.isAfter(tIni)) return ""
    val minutos = java.time.Duration.between(tIni, tFin).toMinutes()
    return if (minutos >= 60) {
        val h = minutos / 60
        val m = minutos % 60
        if (m == 0L) "${h}h" else "${h}h ${m}m"
    } else "${minutos}m"
}

// ── Colores compartidos para OutlinedTextField ────────────────────────────────
@Composable
private fun campoColores() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor     = GuindaPrimario,
    unfocusedBorderColor   = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    focusedLabelColor      = GuindaPrimario,
    cursorColor            = GuindaPrimario,
    errorBorderColor       = MaterialTheme.colorScheme.error,
    focusedContainerColor  = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)