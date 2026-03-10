package com.example.app_sisaep.view.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.app_sisaep.model.dto.AvisoGlobal
import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import com.example.app_sisaep.view.navigation.Routes
import com.example.app_sisaep.view.screens.avsos.FormularioAviso
import com.example.app_sisaep.view.screens.noticias.InicioNoticias
import com.example.app_sisaep.viewModel.AuthApp.obtenerNombreUsuario
import com.example.app_sisaep.viewModel.crearAviso
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {

    var selectedIndex by remember { mutableIntStateOf(0) }

    var esAdmin by remember { mutableStateOf(false) }

    var mostrarFormularioAviso by remember { mutableStateOf(false) }

    var avisoActual by remember { mutableStateOf<AvisoGlobal?>(null) }
    var mostrarAviso by remember { mutableStateOf(false) }

    var nombreUsuario by remember { mutableStateOf("Usuario") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {

        nombreUsuario = obtenerNombreUsuario()

        // Verificar si el usuario está en personal_administrativo
        try {

            val user = SupabaseConnectionApp.client.auth.currentUserOrNull()

            if (user != null) {

                val resultado = SupabaseConnectionApp.client
                    .from("personal_administrativo")
                    .select {
                        filter {
                            eq("id", user.id)
                        }
                    }
                    .decodeList<Map<String, String>>()

                println("USER ID: ${user.id}")
                println("RESULTADO ADMIN: $resultado")

                esAdmin = resultado.isNotEmpty()

            } else {

                esAdmin = false

            }

        } catch (e: Exception) {

            println("ERROR CONSULTA ADMIN: ${e.message}")
            esAdmin = false

        }

        println("USER ID: ${SupabaseConnectionApp.client.auth.currentUserOrNull()?.id}")
        println("ES ADMIN: $esAdmin")

    }

    val navItems = listOf(
        BottomNavItem("Inicio") { Icon(Icons.Filled.Home, null) },
        BottomNavItem("Calendario") { Icon(Icons.Filled.CalendarMonth, null) },
        BottomNavItem("Agenda") { Icon(Icons.Filled.Schedule, null) },
        BottomNavItem("Clases") { Icon(Icons.Filled.School, null) },
    )

    if (mostrarAviso && avisoActual != null) {

        AlertDialog(
            onDismissRequest = { mostrarAviso = false },

            confirmButton = {
                Button(
                    onClick = { mostrarAviso = false }
                ) {
                    Text("Entendido")
                }
            },

            title = { Text(avisoActual!!.titulo) },

            text = { Text(avisoActual!!.mensaje) }
        )

    }

    AppScaffold(

        nombreUsuario = nombreUsuario,

        selectedIndex = selectedIndex,

        onItemSelected = { index ->

            selectedIndex = index

            when (index) {
                0 -> navController.navigate(Routes.Home)
                1 -> navController.navigate(Routes.Calendario)
                2 -> navController.navigate(Routes.Agenda)
                3 -> navController.navigate(Routes.Clases)
            }

        },

        onGenerateQrClick = {
            navController.navigate(Routes.GenerarQR)
        },

        onReadQrClick = {
            navController.navigate(Routes.ScanQR)
        },

        onConfigClick = {
            navController.navigate("config")
        },

        onLogoutClick = {
            navController.navigate(Routes.Login) {
                popUpTo(0)
                launchSingleTop = true
            }
        },

        navItems = navItems,

        floatingActionButton = {

            if (esAdmin && !mostrarFormularioAviso) {

                FloatingActionButton(
                    onClick = {
                        mostrarFormularioAviso = true
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Crear aviso"
                    )

                }

            }

        }

    ) { innerPadding ->

        AnimatedContent(

            targetState = mostrarFormularioAviso,

            transitionSpec = {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            },

            modifier = Modifier.padding(innerPadding),

            label = "homeAnimacion"

        ) { mostrarFormulario ->

            if (mostrarFormulario) {

                FormularioAviso(

                    onCancelar = {
                        mostrarFormularioAviso = false
                    },

                    onPublicar = { titulo, mensaje, tipo, fecha ->

                        scope.launch {

                            crearAviso(
                                titulo,
                                mensaje,
                                tipo,
                                fecha
                            )

                            mostrarFormularioAviso = false

                        }

                    }

                )

            } else {

                InicioNoticias()

            }

        }

    }

}