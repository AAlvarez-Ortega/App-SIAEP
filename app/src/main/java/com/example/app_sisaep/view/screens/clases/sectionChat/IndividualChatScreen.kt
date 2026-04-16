package com.example.app_sisaep.view.screens.clases.sectionChat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_sisaep.R
import com.example.app_sisaep.model.dto.MensajeDto
import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import com.example.app_sisaep.viewModel.consultaas
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualChatScreen(navController: NavController, receiverId: String, receiverName: String) {
    val scope = rememberCoroutineScope()
    var conversacionId by remember { mutableStateOf<String?>(null) }
    var listaMensajes by remember { mutableStateOf<List<MensajeDto>>(emptyList()) }
    var nuevoMensajeTexto by remember { mutableStateOf("") }

    LaunchedEffect(receiverId) {
        val miId = SupabaseConnectionApp.client.auth.currentUserOrNull()?.id
        if (miId != null) {
            val id = consultaas.obtenerOCrearConversacion(miId, receiverId)
            conversacionId = id

            if (id != null) {
                listaMensajes = consultaas.obtenerMensajes(id)

                val canalChat = SupabaseConnectionApp.client.realtime.channel("chat_$id")
                val flujo = canalChat.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "mensajes"
                    filter = "conversacion_id=eq.$id"
                }
                canalChat.subscribe()
                flujo.collect { action ->
                    val nuevo = action.decodeRecord<MensajeDto>()
                    if (listaMensajes.none { it.id == nuevo.id }) {
                        listaMensajes = listaMensajes + nuevo
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(receiverName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listaMensajes) { msj ->
                    val esMio = msj.remitente_id != receiverId
                    ChatBubble(text = msj.contenido, esMio = esMio)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nuevoMensajeTexto,
                    onValueChange = { nuevoMensajeTexto = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.message)) },
                    shape = RoundedCornerShape(25.dp)
                )

                IconButton(
                    onClick = {
                        val idConv = conversacionId
                        val miId = SupabaseConnectionApp.client.auth.currentUserOrNull()?.id
                        if (idConv != null && miId != null && nuevoMensajeTexto.isNotBlank()) {
                            scope.launch {
                                val msj = MensajeDto(
                                    conversacion_id = idConv,
                                    remitente_id = miId,
                                    contenido = nuevoMensajeTexto
                                )
                                if (consultaas.enviarMensaje(msj)) {
                                    nuevoMensajeTexto = ""
                                }
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.send)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, esMio: Boolean) {
    val bubbleColor = if (esMio) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (esMio) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (esMio) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 1.dp
        ) {
            Text(
                text = text,
                color = textColor,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}