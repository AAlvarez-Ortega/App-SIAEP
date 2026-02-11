package com.example.app_sisaep.view.screens

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_sisaep.model.supabase.consultaas
import com.example.app_sisaep.model.supabase.estatus
import com.example.app_sisaep.model.dto.EscuelaDto
import com.example.app_sisaep.model.dto.SolicitudInsertDto
import com.example.app_sisaep.view.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreRegistroScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var escuelas by remember { mutableStateOf<List<EscuelaDto>>(emptyList()) }
    var escuelaSeleccionada by remember { mutableStateOf<EscuelaDto?>(null) }
    var expanded by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var apellidoPaterno by remember { mutableStateOf("") }
    var apellidoMaterno by remember { mutableStateOf("") }
    var numeroBoleta by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var curp by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        try {
            escuelas = consultaas.getEscuelas()
        } catch (e: Exception) {
            error = e.message ?: "Error al cargar escuelas"
        } finally {
            loading = false
        }
    }

    fun validarFormulario(): String? {
        if (nombre.isBlank()) return "Ingresa tu nombre"
        if (apellidoPaterno.isBlank()) return "Ingresa tu apellido paterno"
        if (apellidoMaterno.isBlank()) return "Ingresa tu apellido materno"
        if (numeroBoleta.isBlank()) return "Ingresa tu número de boleta"
        if (correo.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) return "Ingresa un correo válido"
        if (curp.isBlank() || curp.length < 18) return "Ingresa tu CURP (18 caracteres)"
        if (escuelaSeleccionada == null) return "Selecciona una escuela"
        return null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pre-registro") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = apellidoPaterno,
                onValueChange = { apellidoPaterno = it },
                label = { Text("Apellido paterno") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = apellidoMaterno,
                onValueChange = { apellidoMaterno = it },
                label = { Text("Apellido materno") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = numeroBoleta,
                onValueChange = { numeroBoleta = it },
                label = { Text("Número de boleta o empleado") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = curp,
                onValueChange = { curp = it.uppercase() },
                label = { Text("CURP") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            // Combo de escuelas
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = escuelaSeleccionada?.let { "${it.nombre}${if (it.siglas.isNullOrBlank()) "" else " (${it.siglas})"}" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Escuela") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    escuelas.forEach { escuela ->
                        DropdownMenuItem(
                            text = { Text("${escuela.nombre}${if (escuela.siglas.isNullOrBlank()) "" else " (${escuela.siglas})"}") },
                            onClick = {
                                escuelaSeleccionada = escuela
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val msg = validarFormulario()
                    if (msg != null) {
                        error = msg
                        return@Button
                    }

                    sending = true
                    error = null

                    scope.launch {
                        try {
                            val payload = SolicitudInsertDto(
                                nombre = nombre.trim(),
                                apellidoPaterno = apellidoPaterno.trim(),
                                apellidoMaterno = apellidoMaterno.trim(),
                                numeroBoleta = numeroBoleta.trim(),
                                correo = correo.trim(),
                                curp = curp.trim(),
                                escuelaId = escuelaSeleccionada!!.id
                            )

                            val newId = consultaas.insertarSolicitud(payload)
                            estatus.guardarSolicitudPendiente(context, newId)

                            // regresa al login y bloquea ahí con "solicitud en proceso"
                            navController.navigate(Routes.Login) {
                                popUpTo(Routes.Login) { inclusive = true }
                            }
                        } catch (e: Exception) {
                            error = e.message ?: "No se pudo enviar la solicitud"
                        } finally {
                            sending = false
                        }
                    }
                },
                enabled = !sending && !loading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (sending) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Enviando...")
                } else {
                    Text("Enviar pre-registro")
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Volver al login",
                modifier = Modifier.clickable { navController.popBackStack() }
            )
        }
    }
}

