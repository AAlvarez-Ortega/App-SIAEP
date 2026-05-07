package com.example.app_sisaep.view.screens.clases

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class TareaProfesorDummy(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val requisitos: String,
    val fechaEntrega: String,
    val entregadas: Int,
    val totalAlumnos: Int
)

data class EntregaAlumnoDummy(
    val nombreAlumno: String,
    val entrego: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasProfesorClaseScreen(
    navController: NavController,
    claseId: String,
    claseNombre: String
) {

    var tareaSeleccionada by remember {
        mutableStateOf<TareaProfesorDummy?>(null)
    }

    val guinda = Color(0xFF800020)
    val guindaSuave = Color(0xFFF3D6DC)

    val tareas = listOf(
        TareaProfesorDummy(
            id = "1",
            titulo = "Tarea 1: Conjuntos",
            descripcion = "Resolver ejercicios relacionados con conjuntos, subconjuntos, unión, intersección y diferencia.",
            requisitos = "Entregar en PDF, incluir procedimiento completo y nombre del alumno en la primera hoja.",
            fechaEntrega = "Viernes",
            entregadas = 18,
            totalAlumnos = 25
        ),
        TareaProfesorDummy(
            id = "2",
            titulo = "Actividad de lógica",
            descripcion = "Completar ejercicios de proposiciones, conectores lógicos, implicaciones y equivalencias.",
            requisitos = "Subir evidencia clara, ordenada y con todas las tablas de verdad completas.",
            fechaEntrega = "Lunes",
            entregadas = 12,
            totalAlumnos = 25
        ),
        TareaProfesorDummy(
            id = "3",
            titulo = "Tablas de verdad",
            descripcion = "Construir tablas de verdad para las expresiones indicadas durante la clase.",
            requisitos = "Entregar archivo PDF o imagen legible. Debe incluir desarrollo paso a paso.",
            fechaEntrega = "Miércoles",
            entregadas = 21,
            totalAlumnos = 25
        )
    )

    val alumnos = listOf(
        EntregaAlumnoDummy("Ana López", true),
        EntregaAlumnoDummy("Carlos Pérez", false),
        EntregaAlumnoDummy("David Mora", true),
        EntregaAlumnoDummy("María González", false),
        EntregaAlumnoDummy("Luis Hernández", true)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

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
                                text = if (tareaSeleccionada == null)
                                    "Tareas del profesor"
                                else
                                    tareaSeleccionada!!.titulo,
                                fontWeight = FontWeight.Black,
                                fontSize = 23.sp,
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
                                if (tareaSeleccionada != null) {
                                    tareaSeleccionada = null
                                } else {
                                    navController.popBackStack()
                                }
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
        }

    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = 18.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            if (tareaSeleccionada == null) {
                items(tareas) { tarea ->
                    TareaProfesorCard(
                        tarea = tarea,
                        guinda = guinda,
                        guindaSuave = guindaSuave
                    ) {
                        tareaSeleccionada = tarea
                    }
                }
            } else {
                item {
                    DetalleTareaProfesorCard(
                        tarea = tareaSeleccionada!!,
                        guinda = guinda,
                        guindaSuave = guindaSuave
                    )
                }

                item {
                    Text(
                        text = "Entregas de alumnos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(alumnos) { alumno ->
                    AlumnoEntregaCard(
                        alumno = alumno,
                        guinda = guinda
                    )
                }
            }
        }
    }
}

@Composable
fun TareaProfesorCard(
    tarea: TareaProfesorDummy,
    guinda: Color,
    guindaSuave: Color,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    tint = guinda
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tarea.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Entrega: ${tarea.fechaEntrega}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${tarea.entregadas}/${tarea.totalAlumnos} entregadas",
                    color = guinda,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DetalleTareaProfesorCard(
    tarea: TareaProfesorDummy,
    guinda: Color,
    guindaSuave: Color
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
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
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = guinda
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = tarea.titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Entrega: ${tarea.fechaEntrega}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Descripción",
                fontWeight = FontWeight.Bold,
                color = guinda
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tarea.descripcion,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Requisitos",
                fontWeight = FontWeight.Bold,
                color = guinda
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tarea.requisitos,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = guindaSuave,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${tarea.entregadas}/${tarea.totalAlumnos} entregadas",
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 7.dp
                    ),
                    color = guinda,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AlumnoEntregaCard(
    alumno: EntregaAlumnoDummy,
    guinda: Color
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = if (alumno.entrego)
                    Icons.Default.CheckCircle
                else
                    Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (alumno.entrego)
                    guinda
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = alumno.nombreAlumno,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (alumno.entrego)
                        "Tarea entregada"
                    else
                        "No ha entregado",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}