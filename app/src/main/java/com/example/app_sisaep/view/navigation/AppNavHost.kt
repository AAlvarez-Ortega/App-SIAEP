package com.example.app_sisaep.view.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.app_sisaep.view.screens.*
import com.example.app_sisaep.view.screens.chat.ChatGrupoScreen
import com.example.app_sisaep.view.screens.clases.CrearAvisoClaseScreen
import com.example.app_sisaep.view.screens.clases.TablonClaseScreen
import com.example.app_sisaep.view.screens.clases.TareasProfesorClaseScreen
import com.example.app_sisaep.view.screens.clases.sectionChat.IndividualChatScreen
import com.example.app_sisaep.view.screens.perfilUsuario.PerfilUsuarioScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {

    NavHost(
        navController = navController,
        startDestination = Routes.Login
    ) {

        composable(Routes.Login) {
            LoginScreen(navController = navController)
        }

        composable(Routes.PreRegistro) {
            PreRegistroScreen(navController = navController)
        }

        composable(Routes.Home) {
            HomeScreen(navController = navController)
        }

        composable(Routes.Calendario) {
            CalendarioScreen(navController = navController)
        }

        composable(Routes.Agenda) {
            AgendaScreen(navController = navController)
        }

        composable(Routes.Clases) {
            ClasesScreen(navController = navController)
        }

        composable(
            route = "tablon_clase/{claseId}/{claseNombre}"
        ) { backStackEntry ->

            val claseId =
                backStackEntry.arguments?.getString("claseId") ?: ""

            val claseNombre =
                backStackEntry.arguments?.getString("claseNombre") ?: "Clase"

            TablonClaseScreen(
                navController = navController,
                claseId = claseId,
                claseNombre = claseNombre
            )
        }

        composable(
            route = "tareas_profesor_clase/{claseId}/{claseNombre}"
        ) { backStackEntry ->

            val claseId =
                backStackEntry.arguments?.getString("claseId") ?: ""

            val claseNombre =
                backStackEntry.arguments?.getString("claseNombre") ?: "Clase"

            TareasProfesorClaseScreen(
                navController = navController,
                claseId = claseId,
                claseNombre = claseNombre
            )
        }

        composable(
            route = "crear_aviso_clase/{claseId}/{claseNombre}"
        ) { backStackEntry ->

            val claseId =
                backStackEntry.arguments?.getString("claseId") ?: ""

            val claseNombre =
                backStackEntry.arguments?.getString("claseNombre") ?: "Clase"

            CrearAvisoClaseScreen(
                navController = navController,
                claseId = claseId,
                claseNombre = claseNombre
            )
        }

        composable(
            route = "chat_grupo/{grupoId}/{grupoNombre}"
        ) { backStackEntry ->

            val grupoId =
                backStackEntry.arguments?.getString("grupoId") ?: ""

            val grupoNombre =
                backStackEntry.arguments?.getString("grupoNombre") ?: "Grupo"

            ChatGrupoScreen(
                navController = navController,
                grupoId = grupoId,
                grupoNombre = grupoNombre
            )
        }

        composable(Routes.GenerarQR) {
            GenerarQrScreen(navController = navController)
        }

        composable(Routes.ScanQR) {
            ScanQrScreen(navController = navController)
        }

        composable(Routes.Config) {
            ConfigScreen(
                navController = navController,
                darkMode = darkMode,
                onDarkModeChange = onDarkModeChange
            )
        }

        composable(
            route = "chat/{userId}/{userName}"
        ) { backStackEntry ->

            val userId =
                backStackEntry.arguments?.getString("userId") ?: ""

            val userName =
                backStackEntry.arguments?.getString("userName") ?: "Usuario"

            IndividualChatScreen(
                navController = navController,
                receiverId = userId,
                receiverName = userName
            )
        }

        composable(Routes.Perfil) {
            PerfilUsuarioScreen(navController = navController)
        }
    }
}