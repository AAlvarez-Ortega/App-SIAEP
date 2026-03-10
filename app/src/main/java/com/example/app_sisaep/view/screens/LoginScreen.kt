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

import androidx.compose.ui.res.stringResource
import com.example.app_sisaep.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // ✅ ahora guardamos estado real (no boolean)
    var estadoSolicitud by remember { mutableStateOf(estatus.EstadoSolicitud.NO_EXISTE) }
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

        // ✅ 2) Si NO hay sesión, consulta estatus (nuevo)
        checkingStatus = true
        statusError = null
        try {
            estadoSolicitud = estatus.obtenerEstadoSolicitud(context)
        } catch (e: Exception) {
            statusError = e.message ?: context.getString(R.string.error_validate_request_status)
            estadoSolicitud = estatus.EstadoSolicitud.NO_EXISTE
        } finally {
            checkingStatus = false
        }
    }

    val bloqueadoPorEstado =
        estadoSolicitud == estatus.EstadoSolicitud.PENDIENTE ||
                estadoSolicitud == estatus.EstadoSolicitud.RECHAZADO

    val bloqueado = bloqueadoPorEstado || checkingStatus || isLoggingIn

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
                text = stringResource(R.string.welcome),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.login_to_continue),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF444444)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (checkingStatus) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(stringResource(R.string.checking_status)) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ✅ Mensajes según estado
            when (estadoSolicitud) {
                estatus.EstadoSolicitud.PENDIENTE -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = stringResource(R.string.request_in_process),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF664D03)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.preregister_sent_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF664D03)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                estatus.EstadoSolicitud.RECHAZADO -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE5E5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = stringResource(R.string.request_rejected),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF8A1F1F)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.request_rejected_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF8A1F1F)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                else -> Unit // ACEPTADO o NO_EXISTE: no mostramos card
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
                label = { Text(stringResource(R.string.email)) },
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
                label = { Text(stringResource(R.string.password)) },
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
                            loginError = context.getString(R.string.error_valid_email)
                            return@launch
                        }
                        if (pass.isBlank()) {
                            loginError = context.getString(R.string.error_enter_password)
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
                                            context.getString(R.string.error_email_not_confirmed)

                                        msg.contains("invalid login") ||
                                                msg.contains("invalid") ||
                                                msg.contains("credentials") ->
                                            context.getString(R.string.error_invalid_credentials)

                                        else -> e.message ?: context.getString(R.string.error_login_failed)
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
                    Text(stringResource(R.string.logging_in))
                } else {
                    Text(stringResource(R.string.login), style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.pre_register),
                    color = if (bloqueado) Color.Gray else Color(0xFF7A003C),
                    modifier = Modifier.clickable(enabled = !bloqueado) {
                        navController.navigate(Routes.PreRegistro)
                    }
                )

                Text(
                    text = stringResource(R.string.forgot_password),
                    color = if (bloqueado) Color.Gray else Color(0xFF7A003C),
                    modifier = Modifier.clickable(enabled = !bloqueado) {
                        // luego: AuthApp.resetPassword(email)
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = stringResource(R.string.refresh_status),
                color = Color(0xFF7A003C),
                modifier = Modifier.clickable(enabled = !checkingStatus && !isLoggingIn) {
                    scope.launch {
                        checkingStatus = true
                        statusError = null
                        try {
                            estadoSolicitud = estatus.obtenerEstadoSolicitud(context)
                        } catch (e: Exception) {
                            statusError = e.message ?: context.getString(R.string.error_validate_status)
                            estadoSolicitud = estatus.EstadoSolicitud.NO_EXISTE
                        } finally {
                            checkingStatus = false
                        }
                    }
                }
            )
        }
    }
}