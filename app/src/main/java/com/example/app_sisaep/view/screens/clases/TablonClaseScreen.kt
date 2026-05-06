
package com.example.app_sisaep.view.screens.clases
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.TopAppBarDefaults

data class AvisoClaseDummy(
    val titulo: String,
    val contenido: String,
    val autor: String,
    val fecha: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablonClaseScreen(
    navController: NavController,
    claseId: String,
    claseNombre: String
) {
    val avisosDummy = listOf(
        AvisoClaseDummy(
            titulo = "Bienvenidos al tablón",
            contenido = "Aquí el profesor podrá publicar anuncios importantes de la clase.",
            autor = "Profesor",
            fecha = "Hoy"
        ),
        AvisoClaseDummy(
            titulo = "Próxima entrega",
            contenido = "Recuerden revisar este apartado para futuras tareas, avisos o materiales.",
            autor = "Profesor",
            fecha = "Hoy"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),

                title = {
                    Column {
                        Text(
                            text = claseNombre,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Tablón de anuncios",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },

                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                        navController.navigate("crear_aviso_clase/$claseId/$claseNombre")
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Crear aviso"
                )
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(avisosDummy) { aviso ->
                AvisoClaseCard(aviso = aviso)
            }
        }
    }
}

@Composable
fun AvisoClaseCard(
    aviso: AvisoClaseDummy
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = aviso.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = aviso.contenido,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${aviso.autor} • ${aviso.fecha}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
