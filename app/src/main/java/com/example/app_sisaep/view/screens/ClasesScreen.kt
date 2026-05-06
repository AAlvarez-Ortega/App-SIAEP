package com.example.app_sisaep.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_sisaep.R
import com.example.app_sisaep.view.navigation.Routes
import com.example.app_sisaep.view.screens.clases.ClaseDummy
import com.example.app_sisaep.view.screens.clases.SectionClases
import com.example.app_sisaep.view.screens.clases.SectionGrupos
import com.example.app_sisaep.view.screens.clases.sectionChat.SectionChat
import com.example.app_sisaep.viewModel.consultaas.obtenerMisDatos

data class SecuenciaDummy(
    val id: String,
    val nombre: String,
    val semestre: Int
)

data class MateriaDummy(
    val id: String,
    val nombre: String,
    val semestre: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClasesScreen(navController: NavController) {

    var selectedIndex by remember { mutableIntStateOf(0) }
    var nombreReal by remember { mutableStateOf("") }

    val secuenciasDisponibles = listOf(
        SecuenciaDummy("1nv10", "1NV10", 1),
        SecuenciaDummy("1nv11", "1NV11", 1),
        SecuenciaDummy("1nm10", "1NM10", 1),
        SecuenciaDummy("1nm11", "1NM11", 1),

        SecuenciaDummy("2nv20", "2NV20", 2),
        SecuenciaDummy("2nv21", "2NV21", 2),
        SecuenciaDummy("2nm20", "2NM20", 2),
        SecuenciaDummy("2nm21", "2NM21", 2)
    )

    val materiasDisponibles = listOf(
        MateriaDummy("n101", "MATEMÁTICAS DISCRETAS", 1),
        MateriaDummy("n102", "FUNDAMENTOS DE FÍSICA", 1),
        MateriaDummy("n103", "FÍSICA GENERAL EXPERIMENTAL", 1),
        MateriaDummy("n104", "COMUNICACIÓN PROFESIONAL INTERDISCIPLINARIA", 1),
        MateriaDummy("n105", "FUNDAMENTOS DE ADMINISTRACIÓN", 1),
        MateriaDummy("n106", "RESPONSABILIDAD SOCIAL Y ÉTICA", 1),
        MateriaDummy("n107", "LÓGICA DE PROGRAMACIÓN", 1),

        MateriaDummy("n201", "CÁLCULO DIFERENCIAL E INTEGRAL", 2),
        MateriaDummy("n202", "PSICOLOGÍA EN EL TRABAJO", 2),
        MateriaDummy("n203", "METODOLOGÍA DE LA INVESTIGACIÓN", 2),
        MateriaDummy("n204", "SISTEMAS DIGITALES", 2),
        MateriaDummy("n205", "APLICACIÓN DE SISTEMAS DIGITALES", 2),
        MateriaDummy("n206", "FUNDAMENTOS DE INGENIERÍA DE SOFTWARE", 2),
        MateriaDummy("n207", "ESTRUCTURA DE DATOS", 2),
        MateriaDummy("n208", "PROGRAMACIÓN DE BAJO NIVEL", 2)
    )

    var expanded by remember { mutableStateOf(false) }

    var secuenciaSeleccionada by remember {
        mutableStateOf<SecuenciaDummy?>(null)
    }

    val materiasDeLaSecuencia =
        materiasDisponibles
            .filter { materia ->
                secuenciaSeleccionada == null ||
                        materia.semestre == secuenciaSeleccionada?.semestre
            }
            .map { materia ->
                ClaseDummy(
                    id = "${materia.id}_${secuenciaSeleccionada?.id ?: "all"}",
                    nombre = materia.nombre
                )
            }

    val navItems = listOf(
        BottomNavItem(stringResource(R.string.home)) {
            androidx.compose.material3.Icon(Icons.Filled.Home, null)
        },

        BottomNavItem(stringResource(R.string.calendar)) {
            androidx.compose.material3.Icon(Icons.Filled.CalendarMonth, null)
        },

        BottomNavItem(stringResource(R.string.agenda)) {
            androidx.compose.material3.Icon(Icons.Filled.Schedule, null)
        },

        BottomNavItem(stringResource(R.string.classes)) {
            androidx.compose.material3.Icon(Icons.Filled.School, null)
        },
    )

    LaunchedEffect(Unit) {
        val usuario = obtenerMisDatos()

        if (usuario != null) {
            nombreReal = usuario.nombre
        }
    }

    AppScaffold(
        nombreUsuario = nombreReal,

        selectedIndex = 3,

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

        onUserClick = {
            navController.navigate(Routes.Perfil)
        },

        navItems = navItems

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),

                verticalAlignment = Alignment.CenterVertically,

                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Mis Clases",

                    style = MaterialTheme.typography.headlineSmall,

                    color = MaterialTheme.colorScheme.onBackground
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,

                    onExpandedChange = {
                        expanded = !expanded
                    }
                ) {

                    OutlinedTextField(
                        value = secuenciaSeleccionada?.nombre ?: "Secuencia",

                        onValueChange = {},

                        readOnly = true,

                        singleLine = true,

                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        },

                        shape = RoundedCornerShape(14.dp),

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,

                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),

                            cursorColor = MaterialTheme.colorScheme.primary
                        ),

                        modifier = Modifier
                            .menuAnchor()
                            .width(150.dp)
                            .height(56.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,

                        onDismissRequest = {
                            expanded = false
                        }
                    ) {

                        secuenciasDisponibles.forEach { secuencia ->

                            DropdownMenuItem(

                                text = {
                                    Text(secuencia.nombre)
                                },

                                onClick = {

                                    secuenciaSeleccionada = secuencia

                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            SectionClases(
                navController = navController,
                clases = materiasDeLaSecuencia
            )

            SectionGrupos(navController)

            SectionChat(navController = navController)
        }
    }
}