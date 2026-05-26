package com.example.app_sisaep.view.screens.clases.sectionChat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.app_sisaep.R
import com.example.app_sisaep.model.dto.ChatPreviewDto
import com.example.app_sisaep.model.dto.UsuarioDto
import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import com.example.app_sisaep.viewModel.consultaas
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.launch

@Composable
fun SectionChat(navController: NavController) {

    var verContactos by remember { mutableStateOf(false) }
    var listaContactos by remember { mutableStateOf<List<UsuarioDto>>(emptyList()) }
    var listaConversaciones by remember { mutableStateOf<List<ChatPreviewDto>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    suspend fun cargarBandeja() {
        try {
            val misDatos = consultaas.obtenerMisDatos()

            if (misDatos != null) {
                listaContactos = consultaas.obtenerContactosPorEscuela(misDatos.escuela_cct)
                listaConversaciones = consultaas.obtenerMisChatsActivosOrdenados()
            }
        } catch (e: Exception) {
            println("Error actualizando bandeja de chat: ${e.message}")
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    cargarBandeja()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        cargando = true

        try {
            val misDatos = consultaas.obtenerMisDatos()

            if (misDatos != null) {
                listaContactos = consultaas.obtenerContactosPorEscuela(misDatos.escuela_cct)
                listaConversaciones = consultaas.obtenerMisChatsActivosOrdenados()
                cargando = false

                val canalMensajes = SupabaseConnectionApp.client.realtime.channel(
                    "bandeja_chats_${misDatos.id_usuario}"
                )

                val flujoMensajes = canalMensajes.postgresChangeFlow<PostgresAction.Insert>(
                    schema = "public"
                ) {
                    table = "mensajes"
                }

                canalMensajes.subscribe()

                flujoMensajes.collect {
                    listaConversaciones = consultaas.obtenerMisChatsActivosOrdenados()
                }
            } else {
                cargando = false
            }
        } catch (e: Exception) {
            println("Error cargando sección chat: ${e.message}")
            cargando = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = verContactos,
            transitionSpec = {
                if (targetState) {
                    slideInHorizontally(animationSpec = tween(400)) { it } + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = tween(400)) { -it } + fadeOut()
                } else {
                    slideInHorizontally(animationSpec = tween(400)) { -it } + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = tween(400)) { it } + fadeOut()
                }
            },
            label = "ChatTransition"
        ) { mostrandoContactos ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    if (mostrandoContactos) {
                        IconButton(onClick = { verContactos = false }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }

                    Text(
                        text = if (mostrandoContactos) {
                            stringResource(R.string.select_contact)
                        } else {
                            stringResource(R.string.messages)
                        },
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (cargando) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (mostrandoContactos) {
                        if (listaContactos.isEmpty()) {
                            EmptyStateMsg(stringResource(R.string.no_contacts_found))
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(listaContactos) { usuario ->
                                    val nombreFull = "${usuario.nombre} ${usuario.apellido_paterno}"

                                    UserChatItem(nombre = nombreFull) {
                                        navController.navigate("chat/${usuario.id_usuario}/$nombreFull")
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 76.dp),
                                        thickness = 0.5.dp,
                                        color = Color.LightGray.copy(0.4f)
                                    )
                                }
                            }
                        }
                    } else {
                        if (listaConversaciones.isEmpty()) {
                            EmptyStateMsg(stringResource(R.string.no_conversations_yet))
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(listaConversaciones) { chat ->
                                    UserChatItem(
                                        nombre = chat.nombreCompleto,
                                        subtitulo = chat.ultimoMensaje
                                    ) {
                                        navController.navigate(
                                            "chat/${chat.usuarioId}/${chat.nombreCompleto}"
                                        )
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 76.dp),
                                        thickness = 0.5.dp,
                                        color = Color.LightGray.copy(0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!verContactos) {
            FloatingActionButton(
                onClick = { verContactos = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.new_chat),
                    tint = Color.White
                )
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.Gray,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}