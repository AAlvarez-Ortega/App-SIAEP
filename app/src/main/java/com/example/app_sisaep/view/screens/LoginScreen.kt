package com.example.app_sisaep.view.screens

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_sisaep.view.navigation.Routes
import com.example.app_sisaep.viewModel.AuthApp
import com.example.app_sisaep.viewModel.RecordarSesion
import com.example.app_sisaep.viewModel.estatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var solicitudEnProceso by remember { mutableStateOf(false) }
    var checkingStatus by remember { mutableStateOf(true) }
    var statusError by remember { mutableStateOf<String?>(null) }

    var isLoggingIn by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    fun isValidEmail(value: String): Boolean {
        val v = value.trim()
        return v.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(v).matches()
    }

    // ✅ 1) Revisar sesión REAL (Supabase) al abrir la pantalla
    LaunchedEffect(Unit) {
        // Espera un poco a que Supabase restaure sesión desde storage (si existe)
        val haySesion = try {
            RecordarSesion.esperarSesion(timeoutMs = 2500L, tickMs = 150L)
        } catch (_: Exception) {
            false
        }

        if (haySesion) {
            navController.navigate(Routes.Home) {
                popUpTo(Routes.Login) { inclusive = true }
            }
            return@LaunchedEffect
        }

        // ✅ 2) Si NO hay sesión, consulta estatus (como antes)
        checkingStatus = true
        statusError = null
        try {
            solicitudEnProceso = estatus.haySolicitudEnProceso(context)
        } catch (e: Exception) {
            statusError = e.message ?: "No se pudo validar el estatus de la solicitud"
            solicitudEnProceso = false
        } finally {
            checkingStatus = false
        }
    }

    val bloqueado = solicitudEnProceso || checkingStatus || isLoggingIn

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF9F9F9)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {

            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF444444)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (checkingStatus) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text("Verificando estatus...") }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (solicitudEnProceso) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Solicitud en proceso",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF664D03)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tu pre-registro ya fue enviado. Cuando sea validado podrás iniciar sesión.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF664D03)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            statusError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            loginError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !bloqueado,
                shape = RoundedCornerShape(12.dp),
                isError = email.isNotBlank() && !isValidEmail(email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7A003C),
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = Color(0xFF7A003C),
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedLabelColor = Color(0xFF7A003C)
                )

            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !bloqueado,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7A003C),
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = Color(0xFF7A003C),
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedLabelColor = Color(0xFF7A003C)
                )

            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    scope.launch {
                        loginError = null

                        val emailClean = email.trim().lowercase()
                        val pass = password

                        if (!isValidEmail(emailClean)) {
                            loginError = "Ingresa un correo válido."
                            return@launch
                        }
                        if (pass.isBlank()) {
                            loginError = "Ingresa tu contraseña."
                            return@launch
                        }

                        isLoggingIn = true
                        try {
                            val result = AuthApp.login(
                                userEmail = emailClean,
                                userPassword = pass
                            )

                            result.fold(
                                onSuccess = {
                                    // ✅ Ya NO guardamos sesión local.
                                    // Supabase persiste sesión por su storage interno.

                                    navController.navigate(Routes.Home) {
                                        popUpTo(Routes.Login) { inclusive = true }
                                    }
                                },
                                onFailure = { e ->
                                    val msg = (e.message ?: "").lowercase()
                                    loginError = when {
                                        msg.contains("email not confirmed") ||
                                                msg.contains("email_not_confirmed") ||
                                                msg.contains("confirm") ->
                                            "Tu correo aún no está confirmado. Revisa tu email y confirma tu cuenta."

                                        msg.contains("invalid login") ||
                                                msg.contains("invalid") ||
                                                msg.contains("credentials") ->
                                            "Credenciales inválidas. Verifica tu correo y contraseña."

                                        else -> e.message ?: "No se pudo iniciar sesión."
                                    }
                                }
                            )
                        } finally {
                            isLoggingIn = false
                        }
                    }
                },
                enabled = !bloqueado,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7A003C),
                    contentColor = Color.White
                )
            ) {
                if (isLoggingIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Iniciando...")
                } else {
                    Text("Iniciar sesión", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pre-registrarme",
                    color = if (bloqueado) Color.Gray else Color(0xFF7A003C),
                    modifier = Modifier.clickable(enabled = !bloqueado) {
                        navController.navigate(Routes.PreRegistro)
                    }
                )

                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = Color(0xFF7A003C),
                    modifier = Modifier.clickable(enabled = !bloqueado) {
                        // luego: AuthApp.resetPassword(email)
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Actualizar estatus",
                color = Color(0xFF7A003C),
                modifier = Modifier.clickable(enabled = !checkingStatus && !isLoggingIn) {
                    scope.launch {
                        checkingStatus = true
                        statusError = null
                        try {
                            solicitudEnProceso = estatus.haySolicitudEnProceso(context)
                        } catch (e: Exception) {
                            statusError = e.message ?: "No se pudo validar el estatus"
                            solicitudEnProceso = false
                        } finally {
                            checkingStatus = false
                        }
                    }
                }
            )
        }
    }
}
