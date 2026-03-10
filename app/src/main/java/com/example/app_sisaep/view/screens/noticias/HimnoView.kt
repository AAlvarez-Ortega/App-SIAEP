package com.example.app_sisaep.view.screens.noticias

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import com.example.app_sisaep.R

import androidx.compose.ui.res.stringResource
// Modelo del JSON
data class KaraokeLine(val timeMs: Int, val line: String)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HimnoIPNCard(isPlaying: MutableState<Boolean>) {
    var expanded by remember { mutableStateOf(false) }
    var currentLineIndex by remember { mutableStateOf(0) }

    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // === Cargar JSON desde res/raw ===
    val karaokeLines: List<KaraokeLine> = remember {
        val inputStream = context.resources.openRawResource(R.raw.himno_ipn_karaoke)
        val jsonText = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
        val jsonArray = JSONArray(jsonText)
        List(jsonArray.length()) { i ->
            val obj = jsonArray.getJSONObject(i)
            KaraokeLine(
                timeMs = obj.getInt("timeMs"),
                line = obj.getString("line")
            )
        }
    }

    // === Sincronización karaoke ===
    LaunchedEffect(isPlaying.value) {
        if (isPlaying.value) {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val position = mediaPlayer?.currentPosition ?: 0
                val newIndex = karaokeLines.indexOfLast { it.timeMs <= position }
                if (newIndex != -1 && newIndex != currentLineIndex) {
                    currentLineIndex = newIndex
                }
                delay(100)
            }
        }
    }

    // === Limpiar el MediaPlayer al salir de la pantalla ===
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying.value = false
        }
    }

    // === Tarjeta principal ===
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable { expanded = true },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1E57)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Karaoke a la izquierda
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                AnimatedContent(
                    targetState = currentLineIndex,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn())
                            .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                    },
                    label = ""
                ) { index ->
                    if (index in karaokeLines.indices) {
                        Text(
                            text = karaokeLines[index].line,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                fontSize = 22.sp
                            ),
                            maxLines = 2
                        )
                    }
                }
            }

            // Botón Play/Pause
            Column(
                modifier = Modifier.width(72.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    if (!isPlaying.value) {
                        isPlaying.value = true
                        mediaPlayer = MediaPlayer.create(context, R.raw.himno_ipn).apply { start() }
                    } else {
                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                        mediaPlayer = null
                        isPlaying.value = false
                        currentLineIndex = 0
                    }
                }) {
                    Icon(
                        imageVector = if (isPlaying.value) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying.value) {
                            stringResource(R.string.pause_anthem)
                        } else {
                            stringResource(R.string.play_anthem)
                        },
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }

    // === Overlay con himno completo ===
    if (expanded) {
        Dialog(onDismissRequest = { expanded = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                            text = stringResource(R.string.ipn_anthem_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF7F1E57)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = karaokeLines.joinToString("\n") { it.line },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { expanded = false },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7F1E57),
                            contentColor = Color.White
                        )
                    ) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        }
    }
}

