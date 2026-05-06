package com.example.app_sisaep.view.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_sisaep.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MensajeGrupoDummy(
    val id: String,
    val autor: String,
    val contenido: String,
    val esMio: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatGrupoScreen(
    navController: NavController,
    grupoId: String,
    grupoNombre: String
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var nuevoMensajeTexto by remember { mutableStateOf("") }

    var listaMensajes by remember {
        mutableStateOf(
            listOf(
                MensajeGrupoDummy(
                    id = "1",
                    autor = "Valeria",
                    contenido = "¿Alguien sabe si dejaron tarea?",
                    esMio = false
                ),
                MensajeGrupoDummy(
                    id = "2",
                    autor = "Marco",
                    contenido = "Sí, creo que era lo de matemáticas.",
                    esMio = false
                ),
                MensajeGrupoDummy(
                    id = "3",
                    autor = "Tú",
                    contenido = "Va, ahorita reviso el tablón.",
                    esMio = true
                )
            )
        )
    }

    suspend fun scrollAlUltimoMensaje() {
        if (listaMensajes.isNotEmpty()) {
            delay(160)
            listState.animateScrollToItem(listaMensajes.lastIndex)
        }
    }

    LaunchedEffect(listaMensajes.size) {
        scrollAlUltimoMensaje()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = grupoNombre)
                        Text(
                            text = "Grupo de chat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = listaMensajes,
                        key = { mensaje -> mensaje.id }
                    ) { msj ->
                        ChatGrupoBubble(
                            autor = msj.autor,
                            text = msj.contenido,
                            esMio = msj.esMio
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nuevoMensajeTexto,
                        onValueChange = { nuevoMensajeTexto = it },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    scope.launch {
                                        delay(350)
                                        if (listaMensajes.isNotEmpty()) {
                                            listState.animateScrollToItem(listaMensajes.lastIndex)
                                        }
                                    }
                                }
                            },
                        placeholder = {
                            Text(stringResource(R.string.message))
                        },
                        shape = RoundedCornerShape(25.dp)
                    )

                    IconButton(
                        onClick = {
                            val texto = nuevoMensajeTexto.trim()

                            if (texto.isNotBlank()) {
                                listaMensajes = listaMensajes + MensajeGrupoDummy(
                                    id = System.currentTimeMillis().toString(),
                                    autor = "Tú",
                                    contenido = texto,
                                    esMio = true
                                )

                                nuevoMensajeTexto = ""

                                scope.launch {
                                    delay(120)
                                    scrollAlUltimoMensaje()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.send)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatGrupoBubble(
    autor: String,
    text: String,
    esMio: Boolean
) {
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
        if (!esMio) {
            Text(
                text = autor,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }

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