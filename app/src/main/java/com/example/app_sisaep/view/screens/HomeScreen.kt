package com.example.app_sisaep.view.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_sisaep.R
import com.example.app_sisaep.view.navigation.Routes
import com.example.app_sisaep.view.screens.Btncreateavisos.BtnCreateAviso
import com.example.app_sisaep.view.screens.noticias.InicioNoticias
import com.example.app_sisaep.viewModel.consultaas.obtenerMisDatos


@Composable
fun HomeScreen(navController: NavController) {
    var nombreReal by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val navItems = listOf(
        BottomNavItem(stringResource(R.string.home)) { androidx.compose.material3.Icon(Icons.Filled.Home, null) },
        BottomNavItem(stringResource(R.string.calendar)) { androidx.compose.material3.Icon(Icons.Filled.CalendarMonth, null) },
        BottomNavItem(stringResource(R.string.agenda)) { androidx.compose.material3.Icon(Icons.Filled.Schedule, null) },
        BottomNavItem(stringResource(R.string.classes)) { androidx.compose.material3.Icon(Icons.Filled.School, null) },
    )

    LaunchedEffect(Unit) {
        val usuario = obtenerMisDatos()
        if (usuario != null) {
            nombreReal = usuario.nombre
        }
    }

    AppScaffold(
        nombreUsuario = nombreReal,
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
        onGenerateQrClick = { navController.navigate(Routes.GenerarQR) },
        onReadQrClick = { navController.navigate(Routes.ScanQR) },
        onConfigClick = { navController.navigate("config") },
        onLogoutClick = {
            navController.navigate(Routes.Login) {
                popUpTo(0)
                launchSingleTop = true
            }
        },
        navItems = navItems
        // IMPORTANTE: Aquí NO pasamos el floatingActionButton al Scaffold
    ) { innerPadding ->

        // Usamos un Box para que el botón flote solo en esta pantalla
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. El contenido principal (Noticias)
            InicioNoticias(
                modifier = Modifier.padding(paddingValues = innerPadding)
            )

            // 2. El botón flotante posicionado manualmente
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Respeta las barras del scaffold
                    .padding(16.dp), // Margen extra para que no pegue a los bordes
                contentAlignment = Alignment.BottomEnd // Lo manda a la esquina inferior derecha
            ) {
                BtnCreateAviso(onClick = {
                    showCreateDialog = true
                })
            }
        }

        if (showCreateDialog) {
            // Aquí irá tu Diálogo de creación
        }
    }
}