package com.example.app_sisaep.view.screens

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
import com.example.app_sisaep.model.supabase.estatus
import com.example.app_sisaep.view.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var boleta by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var solicitudEnProceso by remember { mutableStateOf(false) }
    var checkingStatus by remember { mutableStateOf(true) } // para mostrar cargando
    var statusError by remember { mutableStateOf<String?>(null) }

    // Consulta estatus al abrir la pantalla
    LaunchedEffect(Unit) {
        checkingStatus = true
        statusError = null
        try {
            solicitudEnProceso = estatus.haySolicitudEnProceso(context)
        } catch (e: Exception) {
            // si falla la consulta, no bloqueamos la app, solo avisamos
            statusError = e.message ?: "No se pudo validar el estatus de la solicitud"
            solicitudEnProceso = false
        } finally {
            checkingStatus = false
        }
    }

    val bloqueado = solicitudEnProceso || checkingStatus

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

            // ✅ Aviso de estatus
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = boleta,
                onValueChange = { boleta = it },
                label = { Text("Número de boleta") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !bloqueado,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF7A003C),
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = Color(0xFF7A003C)
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
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF7A003C),
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = Color(0xFF7A003C)
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    // SOLO UI: por ahora navegamos directo a Home
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
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
                Text("Iniciar sesión", style = MaterialTheme.typography.titleMedium)
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
                    modifier = Modifier.clickable { /* luego */ }
                )
            }

            // (Opcional) botón para revalidar estatus manualmente
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Actualizar estatus",
                color = Color(0xFF7A003C),
                modifier = Modifier.clickable(enabled = !checkingStatus) {
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
