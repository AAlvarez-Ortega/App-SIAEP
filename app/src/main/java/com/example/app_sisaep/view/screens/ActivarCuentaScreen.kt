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
import kotlinx.coroutines.delay

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
    var codigo by remember { mutableStateOf("") }
    var esperaReenvio by remember { mutableIntStateOf(0) }
    var aviso by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(esperaReenvio) {
        if (esperaReenvio > 0) { delay(1000); esperaReenvio-- }
    }

    suspend fun solicitudActual(): SolicitudPropiaDto {
        val comprobante = estatus.obtenerComprobante(context)
            ?: throw IllegalStateException("COMPROBANTE_NO_DISPONIBLE")
        val actual = RegistroApp.propia(comprobante)
        check(actual?.estado == "Aceptado") { "SOLICITUD_NO_APROBADA" }
        return actual
    }

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
            Text("El director ya validó tus datos. Crea tu contraseña y confirma el correo con el código que recibirás.")
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
                                val actual = solicitudActual()
                                AuthApp.activarCuenta(actual, password).getOrThrow()
                                enviado = true
                                esperaReenvio = 60
                                password = ""; confirmacion = ""
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                error = ActivacionCuenta.mensajeError(e)
                            } finally { enviando = false }
                        }
                    }) { Text(if (enviando) "Procesando…" else "Crear cuenta y enviar confirmación") }
                    TextButton(enabled = !enviando, onClick = {
                        password = ""; confirmacion = ""; error = null; enviado = true
                    }) { Text("Ya tengo un código de confirmación") }
                } else {
                    Text("Revisa tu correo y escribe completo el código más reciente. Si ya confirmaste tu correo, vuelve al inicio y usa tu contraseña; no necesitas verificarlo otra vez.")
                    OutlinedTextField(value = codigo, onValueChange = { codigo = it.filter { c -> c in '0'..'9' }.take(10) },
                        label = { Text("Código de confirmación") }, enabled = !enviando, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth())
                    Button(enabled = !enviando, onClick = {
                        error = ActivacionCuenta.errorCodigo(codigo)
                        if (error != null) return@Button
                        scope.launch {
                            enviando = true; aviso = null
                            try {
                                AuthApp.confirmarCorreo(solicitudActual(), codigo).getOrThrow()
                                navController.navigate(Routes.Home) { popUpTo(Routes.Login) { inclusive = true } }
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                error = ActivacionCuenta.mensajeError(e)
                            } finally { codigo = ""; enviando = false }
                        }
                    }) { Text(if (enviando) "Verificando…" else "Confirmar correo") }
                    TextButton(enabled = !enviando && esperaReenvio == 0, onClick = {
                        scope.launch {
                            enviando = true; error = null; aviso = null
                            try {
                                AuthApp.reenviarConfirmacion(solicitudActual()).getOrThrow()
                                codigo = ""; esperaReenvio = 60
                                aviso = "Si tu cuenta está pendiente de confirmación, recibirás un código nuevo. Si ya la confirmaste, puedes iniciar sesión."
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                error = ActivacionCuenta.mensajeError(e)
                            } finally { enviando = false }
                        }
                    }) { Text(if (esperaReenvio > 0) "Reenviar código en $esperaReenvio s" else "Reenviar código") }
                }
            }
            aviso?.let { Text(it) }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            TextButton(enabled = !enviando, onClick = {
                navController.navigate(Routes.Login) { popUpTo(Routes.Login) { inclusive = true } }
            }) { Text("Volver a iniciar sesión") }
        }
    }
}
