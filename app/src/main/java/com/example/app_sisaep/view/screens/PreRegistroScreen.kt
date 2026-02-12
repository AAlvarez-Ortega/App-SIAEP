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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_sisaep.model.dto.EscuelaDto
import com.example.app_sisaep.model.dto.SolicitudInsertDto
import com.example.app_sisaep.view.navigation.Routes
import com.example.app_sisaep.viewModel.consultaas
import com.example.app_sisaep.viewModel.estatus
import kotlinx.coroutines.launch

private val Guinda = Color(0xFF7A003C)
private val Bg = Color(0xFFF6F6F8)

private enum class Step(val title: String, val subtitle: String) {
    Personal("Datos personales", "Asegúrate de escribirlos tal como aparecen en tus documentos."),
    Contacto("Contacto", "Usaremos este correo para notificaciones del proceso."),
    Escuela("Institución", "Selecciona la escuela a la que perteneces.")
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

    // ✅ CAMBIO: antes numeroBoleta
    var boletaOEmpleado by remember { mutableStateOf("") }

    var correo by remember { mutableStateOf("") }
    var curp by remember { mutableStateOf("") }

    var currentStep by remember { mutableStateOf(Step.Personal) }

    // Errores por campo (UX clara)
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
                    errNombre = "Requerido"; ok = false
                }
                if (apellidoPaterno.isBlank()) {
                    errApPat = "Requerido"; ok = false
                }
                if (apellidoMaterno.isBlank()) {
                    errApMat = "Requerido"; ok = false
                }

                val be = boletaOEmpleado.trim()
                if (be.isBlank()) {
                    errBoleta = "Requerido"; ok = false
                } else if (!be.all { it.isDigit() } || be.length !in 8..10) {
                    errBoleta = "Debe tener 8, 9 o 10 dígitos"
                    ok = false
                }
            }

            Step.Contacto -> {
                val email = correo.trim()
                if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    errCorreo = "Correo inválido"
                    ok = false
                }
                val c = curp.trim()
                if (c.length != 18) {
                    errCurp = "Debe tener 18 caracteres"
                    ok = false
                }
            }

            Step.Escuela -> {
                if (escuelaSeleccionada == null) {
                    errEscuela = "Selecciona una escuela"
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

    val fieldColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = Guinda,
        focusedLabelColor = Guinda,
        cursorColor = Guinda,
        unfocusedBorderColor = Color(0xFFD0D0D0)
    )

    LaunchedEffect(Unit) {
        loading = true
        errorMessage = null
        try {
            escuelas = consultaas.getEscuelas()
        } catch (e: Exception) {
            errorMessage = e.message ?: "No se pudieron cargar las escuelas"
        } finally {
            loading = false
        }
    }

    Scaffold(
        containerColor = Bg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pre-registro", fontWeight = FontWeight.SemiBold) }
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
                text = "Solicitud de acceso",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E1E1E)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Completa tus datos para enviar tu solicitud. La validación se realizará por la plataforma.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5A5A5A)
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
                    color = Guinda
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
                            "No se pudo continuar",
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text(
                        text = currentStep.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = currentStep.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6A6A6A)
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
                                        onValueChange = { nombre = it; errNombre = null },
                                        label = { Text("Nombre") },
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
                                            onValueChange = { apellidoPaterno = it; errApPat = null },
                                            label = { Text("Apellido paterno") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            isError = errApPat != null,
                                            supportingText = { errApPat?.let { Text(it) } },
                                            colors = fieldColors,
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                                        )
                                        OutlinedTextField(
                                            value = apellidoMaterno,
                                            onValueChange = { apellidoMaterno = it; errApMat = null },
                                            label = { Text("Apellido materno") },
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
                                        label = { Text("Boleta o empleado") },
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
                                        onValueChange = { correo = it; errCorreo = null },
                                        label = { Text("Correo") },
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
                                        onValueChange = { curp = it.uppercase(); errCurp = null },
                                        label = { Text("CURP (18 caracteres)") },
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
                                            label = { Text("Escuela") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth(),
                                            isError = errEscuela != null,
                                            supportingText = { errEscuela?.let { Text(it) } },
                                            colors = fieldColors
                                        )

                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            escuelas.forEach { escuela ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            "${escuela.nombre}${if (escuela.siglas.isNullOrBlank()) "" else " (${escuela.siglas})"}"
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
                                        text = "Al enviar tu solicitud, se iniciará el proceso de validación.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF6A6A6A)
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
                            // Validar TODO antes de enviar
                            val okPersonal = validateStep(Step.Personal)
                            val okContacto = validateStep(Step.Contacto)
                            val okEscuela = validateStep(Step.Escuela)
                            if (!(okPersonal && okContacto && okEscuela)) return@WizardFooter

                            sending = true
                            errorMessage = null

                            scope.launch {
                                try {
                                    // 🔎 Verificamos si ya existe (boleta/empleado + curp)
                                    // ⚠️ Requiere actualizar consultaas.existeSolicitud(...) para usar boleta_o_empleado
                                    val yaExiste = consultaas.existeSolicitud(
                                        boletaOEmpleado = boletaOEmpleado.trim(),
                                        curp = curp.trim()
                                    )

                                    if (yaExiste) {
                                        errorMessage = "Ya existe una solicitud en proceso con estos datos."
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

                                    // ✅ Insertamos si no existe
                                    val newId = consultaas.insertarSolicitud(payload)
                                    estatus.guardarSolicitudPendiente(context, newId)

                                    navController.navigate(Routes.Login) {
                                        popUpTo(Routes.Login) { inclusive = true }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "No se pudo enviar la solicitud"
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
                    text = "Volver al login",
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

    Column(modifier) {
        LinearProgressIndicator(
            progress = { idx.toFloat() / total.toFloat() },
            color = Guinda,
            trackColor = Color(0xFFE7E7EA),
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
                val active = (s == current)

                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            text = "${i + 1}. ${s.title}",
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (active) Color(0xFFFFEFF6) else Color(0xFFF2F2F4),
                        labelColor = if (active) Guinda else Color(0xFF444444),
                        disabledContainerColor = if (active) Color(0xFFFFEFF6) else Color(0xFFF2F2F4),
                        disabledLabelColor = if (active) Guinda else Color(0xFF444444)
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Paso $idx de $total",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6A6A6A)
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
            Text("Atrás")
        }

        if (!isLast) {
            Button(
                onClick = onNext,
                enabled = !loading && !sending,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Guinda)
            ) {
                Text("Siguiente", color = Color.White, fontWeight = FontWeight.SemiBold)
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
                    Text("Enviando...", color = Color.White, fontWeight = FontWeight.SemiBold)
                } else {
                    Text("Enviar solicitud", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
