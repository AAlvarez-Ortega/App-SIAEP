package com.example.app_sisaep.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_sisaep.model.dto.SolicitudPropiaDto
import com.example.app_sisaep.view.navigation.Routes
import com.example.app_sisaep.viewModel.ActivacionCuenta
import com.example.app_sisaep.viewModel.AuthApp
import com.example.app_sisaep.viewModel.RegistroApp
import com.example.app_sisaep.viewModel.estatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun ActivarCuentaScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var solicitud by remember { mutableStateOf<SolicitudPropiaDto?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var enviando by remember { mutableStateOf(false) }
    // La contraseña sólo vive en memoria y se borra tras el envío; no es rememberSaveable.
    var password by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var enviado by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val comprobante = estatus.obtenerComprobante(context)
                ?: throw IllegalStateException("COMPROBANTE_NO_DISPONIBLE")
            val propia = RegistroApp.propia(comprobante)
            if (propia?.estado != "Aceptado") throw IllegalStateException("SOLICITUD_NO_APROBADA")
            solicitud = propia
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            error = "No se pudo verificar una solicitud aprobada. Vuelve al inicio y actualiza su estado."
        } finally { cargando = false }
    }

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Activar mi cuenta", style = MaterialTheme.typography.headlineSmall)
            Text("El director ya validó tus datos. Crea tu contraseña y confirma el correo para iniciar sesión.")
            if (cargando) CircularProgressIndicator()
            solicitud?.let { s ->
                OutlinedTextField(value = s.datos.correo, onValueChange = {}, readOnly = true,
                    label = { Text("Correo aprobado") }, modifier = Modifier.fillMaxWidth())
                if (!enviado) {
                    OutlinedTextField(value = password, onValueChange = { password = it },
                        label = { Text("Contraseña") }, enabled = !enviando, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = confirmacion, onValueChange = { confirmacion = it },
                        label = { Text("Repite la contraseña") }, enabled = !enviando, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth())
                    Button(enabled = !enviando, onClick = {
                        error = ActivacionCuenta.errorPassword(password, confirmacion)
                        if (error != null) return@Button
                        scope.launch {
                            enviando = true
                            try {
                                // Vuelve a consultar el comprobante antes de transmitir la contraseña.
                                val comprobante = estatus.obtenerComprobante(context)
                                    ?: throw IllegalStateException("COMPROBANTE_NO_DISPONIBLE")
                                val actual = RegistroApp.propia(comprobante)
                                if (actual?.estado != "Aceptado") throw IllegalStateException("SOLICITUD_NO_APROBADA")
                                AuthApp.activarCuenta(actual, password).getOrThrow()
                                enviado = true
                                password = ""; confirmacion = ""
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                val msg = e.message.orEmpty().lowercase()
                                error = when {
                                    "already registered" in msg || "user_already_exists" in msg -> "Ya hay una cuenta con este correo. Vuelve al inicio e inicia sesión con tu contraseña."
                                    "email_address_not_authorized" in msg || "email address not authorized" in msg -> "El servidor de correo aún no permite enviar a esta dirección. El administrador debe configurar SMTP; tu aprobación se conserva."
                                    "rate" in msg || "over_email_send_rate_limit" in msg -> "Se alcanzó el límite temporal de correos. Espera antes de volver a intentarlo."
                                    else -> "No se pudo completar la activación. Revisa tu correo antes de reintentar; si ya tienes cuenta, inicia sesión."
                                }
                            } finally { enviando = false }
                        }
                    }) { Text(if (enviando) "Procesando…" else "Crear cuenta y enviar confirmación") }
                } else {
                    Text("La solicitud de activación se procesó. Si era una cuenta nueva, revisa tu correo y abre el enlace de confirmación. Si el correo ya tenía cuenta, usa tu contraseña existente; este formulario no la cambia.")
                }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            TextButton(enabled = !enviando, onClick = {
                navController.navigate(Routes.Login) { popUpTo(Routes.Login) { inclusive = true } }
            }) { Text("Volver a iniciar sesión") }
        }
    }
}
