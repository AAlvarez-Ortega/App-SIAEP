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
import com.example.app_sisaep.R
import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import com.example.app_sisaep.view.navigation.Routes
import com.example.app_sisaep.viewModel.AuthApp
import com.example.app_sisaep.viewModel.QrGenerate
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun GenerarQrScreen(navController: NavController) {

    // BottomNav (igual que las demás pantallas)
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

        val tipoUsuarioReal = try {

            val rows: List<JsonObject> =
                SupabaseConnectionApp.client.postgrest["usuarios"]
                    .select {
                        filter { eq("id", userId) }
                        limit(1)
                    }
                    .decodeList()

            rows.firstOrNull()
                ?.get("tipo_usuario")
                ?.jsonPrimitive
                ?.contentOrNull
                ?.trim()
                ?.lowercase()

        } catch (e: Exception) {
            null
        }

        if (tipoUsuarioReal.isNullOrBlank()) {
            cargando = false
            error = "No se pudo obtener el tipo de usuario."
            return@LaunchedEffect
        }

        // QR dinámico con datos reales
        QrGenerate.dynamicQrLoop(
            userId = userId,
            rol = tipoUsuarioReal
        ) { bitmap ->
            qrBitmap = bitmap
            cargando = false
        }
    }

    AppScaffold(
        selectedIndex = 0, // ✅ aquí marcamos "Inicio" (si prefieres otro, lo cambiamos)
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
        nombreUsuario = "Usuario", // luego lo jalamos real

        topBarLogoRes = R.drawable.ipn,
        onConfigClick = {
            navController.navigate(Routes.Config)
        },
        onLogoutClick = {
            navController.navigate(Routes.Login) {
                popUpTo(0)
                launchSingleTop = true
            }
        },
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
