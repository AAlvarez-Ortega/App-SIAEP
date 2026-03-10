package com.example.app_sisaep.view.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_sisaep.R
import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import com.example.app_sisaep.view.navigation.Routes
import com.example.app_sisaep.view.screens.noticias.InicioNoticias
import com.example.app_sisaep.viewModel.crearAviso
import com.example.app_sisaep.viewModel.esPersonalAdministrativo
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import java.util.Calendar
import kotlin.time.Duration.Companion.hours

@Composable
fun HomeScreen(navController: NavController) {

    var selectedIndex by remember { mutableIntStateOf(0) }
    var esAdmin by remember { mutableStateOf(false) }
    var mostrarFormularioAviso by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        esAdmin = esPersonalAdministrativo(SupabaseConnectionApp.client)
    }

    val navItems = listOf(
        BottomNavItem("Inicio") { Icon(Icons.Filled.Home, null) },
        BottomNavItem("Calendario") { Icon(Icons.Filled.CalendarMonth, null) },
        BottomNavItem("Agenda") { Icon(Icons.Filled.Schedule, null) },
        BottomNavItem("Clases") { Icon(Icons.Filled.School, null) },
    )

    AppScaffold(

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

        topBarLogoRes = R.drawable.ipn,

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
                            crearAviso(titulo, mensaje, tipo, fecha)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormularioAviso(
    onCancelar: () -> Unit,
    onPublicar: (String, String, String, String?) -> Unit
) {

    val guindaIPN = Color(0xFF6A1B2E)

    var titulo by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("informativo") }

    var usarExpiracion by remember { mutableStateOf(false) }

    var fechaExpiracion by remember { mutableStateOf<LocalDate?>(null) }
    var horaExpiracion by remember { mutableStateOf<LocalTime?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val tipos = listOf(
        "informativo",
        "urgente",
        "evento",
        "mantenimiento"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                Text(
                    text = "Nuevo aviso",
                    style = MaterialTheme.typography.headlineSmall,
                    color = guindaIPN
                )

                Divider()

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = mensaje,
                    onValueChange = { mensaje = it },
                    label = { Text("Mensaje") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                Text(
                    text = "Tipo de aviso",
                    style = MaterialTheme.typography.titleMedium,
                    color = guindaIPN
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    tipos.forEach {

                        FilterChip(
                            selected = tipo == it,
                            onClick = { tipo = it },
                            label = {
                                Text(
                                    it.replaceFirstChar { c -> c.uppercase() }
                                )
                            }
                        )

                    }

                }

                Divider()

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Checkbox(
                        checked = usarExpiracion,
                        onCheckedChange = { usarExpiracion = it }
                    )

                    Text("Elegir fecha de expiración")

                }

                if (usarExpiracion) {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        DatePickerDialogExample {
                            fechaExpiracion = it
                        }

                        OutlinedTextField(
                            value = fechaExpiracion?.toString() ?: "",
                            onValueChange = {},
                            label = { Text("Fecha") },
                            readOnly = true,
                            modifier = Modifier.width(150.dp)
                        )

                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        TimePickerDialogExample {
                            horaExpiracion = it
                        }

                        OutlinedTextField(
                            value = horaExpiracion?.toString() ?: "",
                            onValueChange = {},
                            label = { Text("Hora") },
                            readOnly = true,
                            modifier = Modifier.width(150.dp)
                        )

                    }

                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    OutlinedButton(
                        onClick = onCancelar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {

                            val fechaFinal = if (usarExpiracion) {

                                if (fechaExpiracion != null && horaExpiracion != null) {
                                    "${fechaExpiracion}T${horaExpiracion}"
                                } else null

                            } else {

                                val expiracion =
                                    kotlinx.datetime.Clock.System.now() + 24.hours

                                expiracion.toString()

                            }

                            onPublicar(
                                titulo,
                                mensaje,
                                tipo,
                                fechaFinal
                            )

                            scope.launch {
                                snackbarHostState.showSnackbar("Aviso publicado")
                            }

                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = guindaIPN
                        )
                    ) {
                        Text("Publicar")
                    }

                }

            }

        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

    }

}
@Composable
fun DatePickerDialogExample(
    onDateSelected: (LocalDate) -> Unit
) {

    val context = LocalContext.current

    Button(onClick = {

        val calendario = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, day ->

                onDateSelected(
                    LocalDate(year, month + 1, day)
                )

            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)

        ).show()

    }) {

        Text("Seleccionar fecha")

    }

}


@Composable
fun TimePickerDialogExample(
    onTimeSelected: (LocalTime) -> Unit
) {

    val context = LocalContext.current

    Button(onClick = {

        val calendario = Calendar.getInstance()

        TimePickerDialog(
            context,
            { _, hour, minute ->

                onTimeSelected(
                    LocalTime(hour, minute)
                )

            },
            calendario.get(Calendar.HOUR_OF_DAY),
            calendario.get(Calendar.MINUTE),
            true

        ).show()

    }) {

        Text("Seleccionar hora")

    }

}

