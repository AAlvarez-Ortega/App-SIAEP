package com.example.app_sisaep.view.screens.noticias


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.app_sisaep.model.dto.AvisoGlobal
import com.example.app_sisaep.viewModel.consultaas.obtenerAvisosActivos
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun NoticiasSection() {
    Text(
        text = "Noticias",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    val avisosState = remember { mutableStateOf<List<AvisoGlobal>>(emptyList()) }
    LaunchedEffect(Unit) {
        val avisos = obtenerAvisosActivos()
        avisosState.value = avisos
    }

    val listState = rememberLazyListState()
    var scrollEnabled by remember { mutableStateOf(true) }

    // Scroll automático y fluido
    LaunchedEffect(avisosState.value, scrollEnabled) {
        val size = avisosState.value.size
        if (size > 1) {
            var index = 0
            while (true) {
                if (scrollEnabled) {
                    index = (index + 1) % size
                    listState.animateScrollToItem(index, scrollOffset = 0)
                    delay(3000L)
                } else {
                    delay(100L) // espera mientras scroll está desactivado
                }
            }
        }
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        itemsIndexed(avisosState.value) { _, aviso ->
            IconoNoticiaCard(
                aviso = aviso,
                onExpandChanged = { expanded ->
                    scrollEnabled = !expanded // pausa scroll cuando se expande
                }
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IconoNoticiaCard(
    aviso: AvisoGlobal,
    onExpandChanged: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val backgroundColor = when (aviso.tipo_aviso?.lowercase(Locale.getDefault())) {
        "informativo" -> Color(0xFFBBDEFB)
        "urgente" -> Color(0xFFFFCDD2)
        "evento" -> Color(0xFFC8E6C9)
        "mantenimiento" -> Color(0xFFFFE0B2)
        else -> Color(0xFFE0E0E0)
    }

    val icon = when (aviso.tipo_aviso?.lowercase(Locale.getDefault())) {
        "informativo" -> Icons.Default.Info
        "urgente" -> Icons.Default.Warning
        "evento" -> Icons.Default.Event
        "mantenimiento" -> Icons.Default.Build
        else -> Icons.Default.Info
    }

    Card(
        modifier = Modifier
            .size(220.dp)
            .shadow(
                elevation = if (expanded) 14.dp else 6.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                expanded = !expanded
                onExpandChanged(expanded) // pausa o reanuda scroll
            }
            .animateContentSize(animationSpec = tween(durationMillis = 400)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = expanded,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(200))
                }
            ) { isExpanded ->
                if (!isExpanded) {
                    // Icono grande + nombre del aviso antes de expandir
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = aviso.tipo_aviso,
                            tint = Color.Black,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = aviso.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            maxLines = 1
                        )
                    }
                } else {
                    // Contenido completo al expandir
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = aviso.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black
                        )
                        Text(
                            text = aviso.mensaje,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                        aviso.fecha_expiracion?.let { fecha ->
                            Text(
                                text = "Expira: ${formatFecha(fecha)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatFecha(fecha: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = parser.parse(fecha)
        if (date != null) formatter.format(date) else fecha
    } catch (e: Exception) {
        fecha
    }
}