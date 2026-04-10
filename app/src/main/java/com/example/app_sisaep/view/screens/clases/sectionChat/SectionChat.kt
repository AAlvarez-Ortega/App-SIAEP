package com.example.app_sisaep.view.screens.clases.sectionChat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app_sisaep.model.dto.ChatPreviewDto
import com.example.app_sisaep.model.dto.UsuarioDto
import com.example.app_sisaep.viewModel.consultaas

@Composable
fun SectionChat(navController: NavController) {
    val scope = rememberCoroutineScope()

    // 1. Estados
    var verContactos by remember { mutableStateOf(false) }
    var listaContactos by remember { mutableStateOf<List<UsuarioDto>>(emptyList()) }
    var listaConversaciones by remember { mutableStateOf<List<ChatPreviewDto>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // 2. Carga de datos y Realtime
    LaunchedEffect(Unit) {
        cargando = true
        try {
            val misDatos = consultaas.obtenerMisDatos()
            if (misDatos != null) {
                listaContactos = consultaas.obtenerContactosPorEscuela(misDatos.escuela_cct)
                // Aquí es donde fallaba por el permiso de RLS
                listaConversaciones = consultaas.obtenerMisChatsActivosOrdenados()
            }
        } catch (e: Exception) {
            println("Error cargando sección chat: ${e.message}")
        }
        cargando = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 3. Contenido Animado (Scroll hacia la derecha/izquierda)
        AnimatedContent(
            targetState = verContactos,
            transitionSpec = {
                if (targetState) { // Hacia la derecha (Contactos)
                    slideInHorizontally(animationSpec = tween(400)) { it } + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = tween(400)) { -it } + fadeOut()
                } else { // Hacia la izquierda (Chats)
                    slideInHorizontally(animationSpec = tween(400)) { -it } + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = tween(400)) { it } + fadeOut()
                }
            }, label = "ChatTransition"
        ) { mostrandoContactos ->
            Column(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {

                // Cabecera Dinámica
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    if (mostrandoContactos) {
                        IconButton(onClick = { verContactos = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                    Text(
                        text = if (mostrandoContactos) "Seleccionar contacto" else "Mensajes",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (cargando) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (mostrandoContactos) {
                        // VISTA DE CONTACTOS
                        if (listaContactos.isEmpty()) {
                            EmptyStateMsg("No se encontraron contactos")
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(listaContactos) { usuario ->
                                    val nombreFull = "${usuario.nombre} ${usuario.apellido_paterno}"
                                    UserChatItem(nombre = nombreFull) {
                                        navController.navigate("chat/${usuario.id}/$nombreFull")
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(start = 76.dp), thickness = 0.5.dp, color = Color.LightGray.copy(0.4f))
                                }
                            }
                        }
                    } else {
                        // VISTA DE MENSAJES ACTIVOS
                        if (listaConversaciones.isEmpty()) {
                            EmptyStateMsg("Aún no tienes conversaciones")
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(listaConversaciones) { chat ->
                                    UserChatItem(
                                        nombre = chat.nombreCompleto,
                                        subtitulo = chat.ultimoMensaje
                                    ) {
                                        navController.navigate("chat/${chat.usuarioId}/${chat.nombreCompleto}")
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(start = 76.dp), thickness = 0.5.dp, color = Color.LightGray.copy(0.4f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Botón Flotante (+)
        if (!verContactos) {
            FloatingActionButton(
                onClick = { verContactos = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Chat", tint = Color.White)
            }
        }
    }
}

@Composable
fun UserChatItem(
    nombre: String,
    subtitulo: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = nombre.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateMsg(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
    }
}