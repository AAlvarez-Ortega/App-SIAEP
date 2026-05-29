package com.example.app_sisaep.view.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_sisaep.view.navigation.Routes
import com.example.app_sisaep.viewModel.AuthApp
import com.example.app_sisaep.viewModel.QrGenerate
import com.example.app_sisaep.viewModel.consultaas.obtenerMisDatos

@Composable
fun GenerarQrScreen(navController: NavController) {
    var nombreReal by remember { mutableStateOf("") }
    val navItems = listOf(
        BottomNavItem("Inicio") { androidx.compose.material3.Icon(Icons.Filled.Home, null) },
        BottomNavItem("Calendario") { androidx.compose.material3.Icon(Icons.Filled.CalendarMonth, null) },
        BottomNavItem("Agenda") { androidx.compose.material3.Icon(Icons.Filled.Schedule, null) },
        BottomNavItem("Clases") { androidx.compose.material3.Icon(Icons.Filled.School, null) },
    )

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val session = remember { runCatching { AuthApp.requireSession() }.getOrNull() }
    val userId = session?.user?.id // coincide con usuarios.id

    LaunchedEffect(userId) {
        if (userId.isNullOrBlank()) {
            cargando = false
            error = "No hay sesión activa."
            return@LaunchedEffect
        }

        cargando = true
        error = null

        try {
            // 1. Obtenemos el objeto usuario completo (ya normalizado)
            val usuario = obtenerMisDatos()

            if (usuario != null) {
                nombreReal = usuario.nombre

                // 2. Iniciamos el loop del QR usando el ID numérico
                // Corregimos el typo: idTipoUsuario (sin la 't' extra)
                QrGenerate.dynamicQrLoop(
                    userId = userId,
                    idTipoUsuario = usuario.id_tipo_usuario
                ) { bitmap ->
                    qrBitmap = bitmap
                    cargando = false
                }
            } else {
                cargando = false
                error = "No se pudo obtener los datos del usuario."
            }
        } catch (e: Exception) {
            cargando = false
            error = "Error: ${e.message}"
        }
    }

    AppScaffold(
        nombreUsuario = nombreReal,
        selectedIndex = 0,
        onItemSelected = { index ->
            when (index) {
                0 -> navController.navigate(Routes.Home)
                1 -> navController.navigate(Routes.Calendario)
                2 -> navController.navigate(Routes.Agenda)
                3 -> navController.navigate(Routes.Clases)
            }
        },
        onGenerateQrClick = {
            // ya estás en Generar QR
        },
        onReadQrClick = {
            navController.navigate(Routes.ScanQR)
        },



        onConfigClick = {
            navController.navigate(Routes.Config)
        },
        onLogoutClick = {
            navController.navigate(Routes.Login) {
                popUpTo(0)
                launchSingleTop = true
            }
        },
        onUserClick = { navController.navigate(Routes.Perfil) },
        navItems = navItems
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                cargando -> CircularProgressIndicator()
                error != null -> Text(error!!)
                qrBitmap != null -> Image(
                    bitmap = qrBitmap!!.asImageBitmap(),
                    contentDescription = "QR dinámico",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
            }
        }
    }
}
