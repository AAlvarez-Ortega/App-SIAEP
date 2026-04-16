package com.example.app_sisaep.view.screens.perfilUsuario

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.app_sisaep.R
import com.example.app_sisaep.model.dto.UsuarioDto
import com.example.app_sisaep.view.navigation.Routes
import com.example.app_sisaep.view.screens.AppScaffold
import com.example.app_sisaep.view.screens.BottomNavItem
import com.example.app_sisaep.viewModel.consultaas.obtenerMisDatos

@Composable
fun PerfilUsuarioScreen(navController: NavController) {
    var usuario by remember { mutableStateOf<UsuarioDto?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var selectedIndex by remember { mutableIntStateOf(4) }

    val navItems = listOf(
        BottomNavItem(stringResource(R.string.home)) { androidx.compose.material3.Icon(Icons.Filled.Home, null) },
        BottomNavItem(stringResource(R.string.calendar)) { androidx.compose.material3.Icon(Icons.Filled.CalendarMonth, null) },
        BottomNavItem(stringResource(R.string.agenda)) { androidx.compose.material3.Icon(Icons.Filled.Schedule, null) },
        BottomNavItem(stringResource(R.string.classes)) { androidx.compose.material3.Icon(Icons.Filled.School, null) },
    )

    LaunchedEffect(Unit) {
        cargando = true
        val resultado = obtenerMisDatos()
        usuario = resultado
        cargando = false
    }

    AppScaffold(
        nombreUsuario = usuario?.nombre ?: stringResource(R.string.user),
        selectedIndex = selectedIndex,
        onItemSelected = { index ->
            when (index) {
                0 -> navController.navigate(Routes.Home)
                1 -> navController.navigate(Routes.Calendario)
                2 -> navController.navigate(Routes.Agenda)
                3 -> navController.navigate(Routes.Clases)
            }
        },
        onGenerateQrClick = { navController.navigate(Routes.GenerarQR) },
        onReadQrClick = { navController.navigate(Routes.ScanQR) },
        onConfigClick = { navController.navigate(Routes.Config) },
        onLogoutClick = {
            navController.navigate(Routes.Login) {
                popUpTo(0)
                launchSingleTop = true
            }
        },
        onUserClick = {},
        navItems = navItems
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
        ) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                usuario?.let { datos ->
                    DatosPerfilDeUsuario(datos)
                } ?: Text(
                    text = stringResource(R.string.could_not_load_data),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}