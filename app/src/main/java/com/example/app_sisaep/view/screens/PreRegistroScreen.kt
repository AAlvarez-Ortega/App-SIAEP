package com.example.app_sisaep.view.screens

import android.util.Patterns
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_sisaep.R
import com.example.app_sisaep.model.dto.EscuelaDto
import com.example.app_sisaep.model.dto.SolicitudInsertDto
import com.example.app_sisaep.model.supabase.SupabaseConnection
import com.example.app_sisaep.view.navigation.Routes
import com.example.app_sisaep.viewModel.consultaas
import com.example.app_sisaep.viewModel.estatus
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

private val Guinda = Color(0xFF7A003C)

private enum class Step(
    val titleRes: Int,
    val subtitleRes: Int
) {
    Personal(
        R.string.step_personal_title,
        R.string.step_personal_subtitle
    ),
    Contacto(
        R.string.step_contact_title,
        R.string.step_contact_subtitle
    ),
    Escuela(
        R.string.step_school_title,
        R.string.step_school_subtitle
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreRegistroScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var escuelas by remember { mutableStateOf<List<EscuelaDto>>(emptyList()) }
    var escuelaSeleccionada by remember { mutableStateOf<EscuelaDto?>(null) }
    var expanded by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var apellidoPaterno by remember { mutableStateOf("") }
    var apellidoMaterno by remember { mutableStateOf("") }
    var boletaOEmpleado by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var curp by remember { mutableStateOf("") }

    var currentStep by remember { mutableStateOf(Step.Personal) }

    var errNombre by remember { mutableStateOf<String?>(null) }
    var errApPat by remember { mutableStateOf<String?>(null) }
    var errApMat by remember { mutableStateOf<String?>(null) }
    var errBoleta by remember { mutableStateOf<String?>(null) }
    var errCorreo by remember { mutableStateOf<String?>(null) }
    var errCurp by remember { mutableStateOf<String?>(null) }
    var errEscuela by remember { mutableStateOf<String?>(null) }

    fun clearErrors() {
        errorMessage = null
        errNombre = null
        errApPat = null
        errApMat = null
        errBoleta = null
        errCorreo = null
        errCurp = null
        errEscuela = null
    }

    fun validateStep(step: Step): Boolean {
        clearErrors()
        var ok = true

        when (step) {
            Step.Personal -> {
                if (nombre.isBlank()) {
                    errNombre = context.getString(R.string.required)
                    ok = false
                }
                if (apellidoPaterno.isBlank()) {
                    errApPat = context.getString(R.string.required)
                    ok = false
                }
                if (apellidoMaterno.isBlank()) {
                    errApMat = context.getString(R.string.required)
                    ok = false
                }

                val be = boletaOEmpleado.trim()
                if (be.isBlank()) {
                    errBoleta = context.getString(R.string.required)
                    ok = false
                } else if (!be.all { it.isDigit() } || be.length !in 8..10) {
                    errBoleta = context.getString(R.string.employee_or_student_number_error)
                    ok = false
                }
            }

            Step.Contacto -> {
                val email = correo.trim()
                if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    errCorreo = context.getString(R.string.invalid_email)
                    ok = false
                }

                val c = curp.trim()
                if (c.length != 18) {
                    errCurp = context.getString(R.string.curp_length_error)
                    ok = false
                }
            }

            Step.Escuela -> {
                if (escuelaSeleccionada == null) {
                    errEscuela = context.getString(R.string.select_school_error)
                    ok = false
                }
            }
        }
        return ok
    }

    fun nextStep() {
        val order = Step.entries
        val idx = order.indexOf(currentStep)
        if (idx < order.lastIndex) currentStep = order[idx + 1]
    }

    fun prevStep() {
        val order = Step.entries
        val idx = order.indexOf(currentStep)
        if (idx > 0) currentStep = order[idx - 1]
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Guinda,
        focusedLabelColor = Guinda,
        cursorColor = Guinda,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedLeadingIconColor = Guinda,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    LaunchedEffect(Unit) {
        loading = true
        errorMessage = null
        try {
            escuelas = consultaas.getEscuelas()
        } catch (e: Exception) {
            errorMessage = e.message ?: context.getString(R.string.schools_load_error)
        } finally {
            loading = false
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.pre_register_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = onBackgroundColor
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Text(
                text = stringResource(R.string.access_request_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = onBackgroundColor
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.access_request_description),
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryTextColor
            )

            Spacer(Modifier.height(14.dp))

            Stepper(
                current = currentStep,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            if (loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Guinda,
                    trackColor = trackColor
                )
                Spacer(Modifier.height(12.dp))
            }

            errorMessage?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F3)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(R.string.continue_error_title),
                            fontWeight = FontWeight.SemiBold,
                            color = Guinda
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(it, color = Color(0xFF7A1A2E))
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(currentStep.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceColor
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = stringResource(currentStep.subtitleRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor
                    )

                    Spacer(Modifier.height(14.dp))

                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            (fadeIn() togetherWith fadeOut()).using(SizeTransform(clip = false))
                        },
                        label = "wizard"
                    ) { step ->
                        when (step) {
                            Step.Personal -> {
                                Column {
                                    OutlinedTextField(
                                        value = nombre,
                                        onValueChange = {
                                            nombre = it.uppercase()
                                            errNombre = null
                                        },
                                        label = { Text(stringResource(R.string.name_label)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        isError = errNombre != null,
                                        supportingText = { errNombre?.let { Text(it) } },
                                        colors = fieldColors,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                        keyboardActions = KeyboardActions(onNext = { })
                                    )

                                    Spacer(Modifier.height(10.dp))

                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = apellidoPaterno,
                                            onValueChange = {
                                                apellidoPaterno = it.uppercase()
                                                errApPat = null
                                            },
                                            label = { Text(stringResource(R.string.last_name_label)) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            isError = errApPat != null,
                                            supportingText = { errApPat?.let { Text(it) } },
                                            colors = fieldColors,
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                                        )

                                        OutlinedTextField(
                                            value = apellidoMaterno,
                                            onValueChange = {
                                                apellidoMaterno = it.uppercase()
                                                errApMat = null
                                            },
                                            label = { Text(stringResource(R.string.middle_name_label)) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            isError = errApMat != null,
                                            supportingText = { errApMat?.let { Text(it) } },
                                            colors = fieldColors,
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                                        )
                                    }

                                    Spacer(Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = boletaOEmpleado,
                                        onValueChange = { input ->
                                            boletaOEmpleado = input.filter { it.isDigit() }.take(10)
                                            errBoleta = null
                                        },
                                        label = {
                                            Text(stringResource(R.string.student_or_employee_number_label))
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        isError = errBoleta != null,
                                        supportingText = { errBoleta?.let { Text(it) } },
                                        colors = fieldColors,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(onDone = {
                                            if (validateStep(Step.Personal)) nextStep()
                                        })
                                    )
                                }
                            }

                            Step.Contacto -> {
                                Column {
                                    OutlinedTextField(
                                        value = correo,
                                        onValueChange = {
                                            correo = it
                                            errCorreo = null
                                        },
                                        label = { Text(stringResource(R.string.email_label)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        isError = errCorreo != null,
                                        supportingText = { errCorreo?.let { Text(it) } },
                                        colors = fieldColors,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Email,
                                            imeAction = ImeAction.Next
                                        )
                                    )

                                    Spacer(Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = curp,
                                        onValueChange = {
                                            curp = it.uppercase()
                                            errCurp = null
                                        },
                                        label = { Text(stringResource(R.string.curp_label)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        isError = errCurp != null,
                                        supportingText = { errCurp?.let { Text(it) } },
                                        colors = fieldColors,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Text,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(onDone = {
                                            if (validateStep(Step.Contacto)) nextStep()
                                        })
                                    )
                                }
                            }

                            Step.Escuela -> {
                                Column {
                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = { if (!loading) expanded = !expanded }
                                    ) {
                                        OutlinedTextField(
                                            value = escuelaSeleccionada?.let {
                                                "${it.nombre}${if (it.siglas.isNullOrBlank()) "" else " (${it.siglas})"}"
                                            } ?: "",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text(stringResource(R.string.school_label)) },
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                            },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth(),
                                            isError = errEscuela != null,
                                            supportingText = { errEscuela?.let { Text(it) } },
                                            colors = fieldColors
                                        )

                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false },
                                            containerColor = surfaceColor
                                        ) {
                                            escuelas.forEach { escuela ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = "${escuela.nombre}${if (escuela.siglas.isNullOrBlank()) "" else " (${escuela.siglas})"}",
                                                            color = onSurfaceColor
                                                        )
                                                    },
                                                    onClick = {
                                                        escuelaSeleccionada = escuela
                                                        errEscuela = null
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Text(
                                        text = stringResource(R.string.validation_process_message),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = secondaryTextColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    WizardFooter(
                        currentStep = currentStep,
                        loading = loading,
                        sending = sending,
                        onBack = { prevStep() },
                        onNext = {
                            val ok = validateStep(currentStep)
                            if (ok) nextStep()
                        },
                        onSubmit = {
                            val okPersonal = validateStep(Step.Personal)
                            val okContacto = validateStep(Step.Contacto)
                            val okEscuela = validateStep(Step.Escuela)

                            if (!(okPersonal && okContacto && okEscuela)) return@WizardFooter

                            sending = true
                            errorMessage = null

                            scope.launch {
                                try {
                                    val yaExiste = consultaas.existeSolicitud(
                                        boletaOEmpleado = boletaOEmpleado.trim(),
                                        curp = curp.trim()
                                    )

                                    if (yaExiste) {
                                        errorMessage = context.getString(R.string.duplicate_request_error)
                                        return@launch
                                    }

                                    val payload = SolicitudInsertDto(
                                        nombre = nombre.trim(),
                                        apellidoPaterno = apellidoPaterno.trim(),
                                        apellidoMaterno = apellidoMaterno.trim(),
                                        boletaOEmpleado = boletaOEmpleado.trim(),
                                        correo = correo.trim(),
                                        curp = curp.trim(),
                                        escuelaId = escuelaSeleccionada!!.id
                                    )

                                    val newId = consultaas.insertarSolicitud(payload)

                                    estatus.guardarSolicitudPendiente(context, newId)

                                    try {
                                        SupabaseConnection.client.auth.signOut()
                                    } catch (_: Exception) {
                                    }

                                    navController.navigate(Routes.Login) {
                                        popUpTo(Routes.PreRegistro) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: context.getString(R.string.submit_request_error)
                                } finally {
                                    sending = false
                                }
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.back_to_login),
                    color = Guinda,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun Stepper(current: Step, modifier: Modifier = Modifier) {
    val steps = Step.entries
    val idx = steps.indexOf(current) + 1
    val total = steps.size

    val secondaryTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val inactiveChipColor = MaterialTheme.colorScheme.surfaceVariant
    val inactiveChipTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier) {
        LinearProgressIndicator(
            progress = { idx.toFloat() / total.toFloat() },
            color = Guinda,
            trackColor = trackColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            steps.forEachIndexed { i, s ->
                val active = s == current

                val w = when (s) {
                    Step.Contacto -> 0.95f
                    Step.Escuela -> 1.05f
                    else -> 1f
                }

                AssistChip(
                    modifier = Modifier.weight(w),
                    onClick = {},
                    enabled = false,
                    label = {
                        val isPersonal = s == Step.Personal

                        val labelText = if (isPersonal) {
                            stringResource(R.string.step_chip_personal_multiline)
                        } else {
                            stringResource(s.titleRes)
                        }

                        val finalText = stringResource(
                            R.string.step_chip_format,
                            i + 1,
                            labelText
                        )

                        Text(
                            text = finalText,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = if (isPersonal) 2 else 1,
                            softWrap = isPersonal
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (active) Guinda.copy(alpha = 0.10f) else inactiveChipColor,
                        labelColor = if (active) Guinda else inactiveChipTextColor,
                        disabledContainerColor = if (active) Guinda.copy(alpha = 0.10f) else inactiveChipColor,
                        disabledLabelColor = if (active) Guinda else inactiveChipTextColor
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.step_counter, idx, total),
            style = MaterialTheme.typography.bodySmall,
            color = secondaryTextColor
        )
    }
}

@Composable
private fun WizardFooter(
    currentStep: Step,
    loading: Boolean,
    sending: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    val isFirst = currentStep == Step.Personal
    val isLast = currentStep == Step.Escuela

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onBack,
            enabled = !isFirst && !sending,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Guinda),
            border = BorderStroke(1.dp, Guinda.copy(alpha = 0.4f))
        ) {
            Text(stringResource(R.string.back_button))
        }

        if (!isLast) {
            Button(
                onClick = onNext,
                enabled = !loading && !sending,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Guinda)
            ) {
                Text(
                    text = stringResource(R.string.next_button),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Button(
                onClick = onSubmit,
                enabled = !loading && !sending,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Guinda)
            ) {
                if (sending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.sending_button),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = stringResource(R.string.submit_request_button),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}