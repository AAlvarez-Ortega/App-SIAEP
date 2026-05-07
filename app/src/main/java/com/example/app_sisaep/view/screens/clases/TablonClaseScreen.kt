package com.example.app_sisaep.view.screens.clases

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class AvisoClaseDummy(
    val titulo: String,
    val contenido: String,
    val autor: String,
    val fecha: String,
    val tipo: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablonClaseScreen(
    navController: NavController,
    claseId: String,
    claseNombre: String
) {

    var opcionSeleccionada by remember {
        mutableStateOf("General")
    }

    val isDarkMode = isSystemInDarkTheme()
    val guinda = Color(0xFF800020)
    val guindaSuave = Color(0xFFF3D6DC)

    val fondo = if (isDarkMode)
        MaterialTheme.colorScheme.background
    else
        Color(0xFFF7F6FA)

    val cardColor = MaterialTheme.colorScheme.surface
    val textoPrincipal = MaterialTheme.colorScheme.onSurface
    val textoSecundario = MaterialTheme.colorScheme.onSurfaceVariant

    val avisosGeneral = listOf(
        AvisoClaseDummy(
            titulo = "Bienvenidos al tablón",
            contenido = "Aquí el profesor podrá publicar anuncios importantes de la clase.",
            autor = "Profesor",
            fecha = "Hoy",
            tipo = "General"
        ),
        AvisoClaseDummy(
            titulo = "Próxima entrega",
            contenido = "Recuerden revisar este apartado para futuras tareas, avisos o materiales.",
            autor = "Profesor",
            fecha = "Hoy",
            tipo = "General"
        )
    )

    val tareas = listOf(
        AvisoClaseDummy(
            titulo = "Tarea 1: Conjuntos",
            contenido = "Resolver ejercicios de conjuntos y relaciones antes del viernes.",
            autor = "Profesor",
            fecha = "Mañana",
            tipo = "Tarea"
        ),
        AvisoClaseDummy(
            titulo = "Actividad de lógica",
            contenido = "Completar tablas de verdad y equivalencias lógicas.",
            autor = "Profesor",
            fecha = "2 días",
            tipo = "Tarea"
        )
    )

    val archivos = listOf(
        AvisoClaseDummy(
            titulo = "Presentación Unidad 1",
            contenido = "Material PDF con todos los temas vistos en clase.",
            autor = "Profesor",
            fecha = "Hoy",
            tipo = "Archivo"
        ),
        AvisoClaseDummy(
            titulo = "Formulario de ejercicios",
            contenido = "Documento con ejercicios para estudiar antes del examen.",
            autor = "Profesor",
            fecha = "Ayer",
            tipo = "Archivo"
        )
    )

    val listaActual = when (opcionSeleccionada) {
        "Tareas" -> tareas
        "Archivos" -> archivos
        else -> avisosGeneral
    }

    Scaffold(
        containerColor = fondo,

        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF5C0011),
                                Color(0xFF9B1B30)
                            )
                        )
                    )
            ) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    title = {
                        Column {
                            Text(
                                text = when (opcionSeleccionada) {
                                    "Tareas" -> "Actividades y entregas"
                                    "Archivos" -> "Materiales de clase"
                                    else -> "Tablón de anuncios"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                color = Color.White
                            )

                            Text(
                                text = claseNombre,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Regresar"
                            )
                        }
                    }
                )
            }
        },

        bottomBar = {
            BarraOpcionesClase(
                opcionSeleccionada = opcionSeleccionada,
                guinda = guinda,
                guindaSuave = guindaSuave,
                cardColor = cardColor,
                textoSecundario = textoSecundario
            ) {
                opcionSeleccionada = it
            }
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (opcionSeleccionada) {
                        "Tareas" -> {
                            navController.navigate(
                                "crear_tarea_clase/$claseId/$claseNombre"
                            )
                        }

                        "Archivos" -> {
                            navController.navigate(
                                "subir_archivo_clase/$claseId/$claseNombre"
                            )
                        }

                        else -> {
                            navController.navigate(
                                "crear_aviso_clase/$claseId/$claseNombre"
                            )
                        }
                    }
                },
                containerColor = guindaSuave,
                contentColor = guinda
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear"
                )
            }
        }

    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),

            contentPadding = PaddingValues(
                top = 18.dp,
                bottom = 24.dp
            ),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (opcionSeleccionada == "Tareas") {
                item {
                    Button(
                        onClick = {
                            navController.navigate(
                                "tareas_profesor_clase/$claseId/$claseNombre"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = guinda,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Ver vista de profesor",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            items(listaActual) { aviso ->
                AvisoClaseCard(
                    aviso = aviso,
                    guinda = guinda,
                    guindaSuave = guindaSuave,
                    cardColor = cardColor,
                    textoPrincipal = textoPrincipal,
                    textoSecundario = textoSecundario
                )
            }
        }
    }
}

@Composable
fun BarraOpcionesClase(
    opcionSeleccionada: String,
    guinda: Color,
    guindaSuave: Color,
    cardColor: Color,
    textoSecundario: Color,
    onSeleccionar: (String) -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(
                    topStart = 28.dp,
                    topEnd = 28.dp
                )
            ),
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        ),
        color = cardColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = 22.dp,
                    vertical = 14.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OpcionBottomClaseItem(
                titulo = "General",
                icono = Icons.Default.Campaign,
                seleccionada = opcionSeleccionada == "General",
                guinda = guinda,
                guindaSuave = guindaSuave,
                textoSecundario = textoSecundario
            ) {
                onSeleccionar("General")
            }

            OpcionBottomClaseItem(
                titulo = "Tareas",
                icono = Icons.Default.Assignment,
                seleccionada = opcionSeleccionada == "Tareas",
                guinda = guinda,
                guindaSuave = guindaSuave,
                textoSecundario = textoSecundario
            ) {
                onSeleccionar("Tareas")
            }

            OpcionBottomClaseItem(
                titulo = "Archivos",
                icono = Icons.Default.Folder,
                seleccionada = opcionSeleccionada == "Archivos",
                guinda = guinda,
                guindaSuave = guindaSuave,
                textoSecundario = textoSecundario
            ) {
                onSeleccionar("Archivos")
            }
        }
    }
}

@Composable
fun OpcionBottomClaseItem(
    titulo: String,
    icono: ImageVector,
    seleccionada: Boolean,
    guinda: Color,
    guindaSuave: Color,
    textoSecundario: Color,
    onClick: () -> Unit
) {

    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(
            horizontal = if (seleccionada) 18.dp else 12.dp,
            vertical = 10.dp
        ),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (seleccionada)
                guindaSuave
            else
                Color.Transparent,

            contentColor = if (seleccionada)
                guinda
            else
                textoSecundario
        )
    ) {
        Icon(
            imageVector = icono,
            contentDescription = titulo,
            modifier = Modifier.size(26.dp)
        )

        if (seleccionada) {
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun AvisoClaseCard(
    aviso: AvisoClaseDummy,
    guinda: Color,
    guindaSuave: Color,
    cardColor: Color,
    textoPrincipal: Color,
    textoSecundario: Color
) {

    val icono = when (aviso.tipo) {
        "Tarea" -> Icons.Default.Assignment
        "Archivo" -> Icons.Default.Description
        else -> Icons.Default.Campaign
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        color = guindaSuave,
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = guinda
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = aviso.titulo,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = textoPrincipal
                    )

                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = textoSecundario
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = aviso.contenido,
                    color = textoSecundario,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = guindaSuave,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = aviso.tipo,
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            ),
                            color = guinda,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "${aviso.autor} • ${aviso.fecha}",
                        color = textoSecundario,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}