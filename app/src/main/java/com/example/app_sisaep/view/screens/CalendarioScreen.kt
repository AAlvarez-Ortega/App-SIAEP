package com.example.app_sisaep.view.screens



import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.app_sisaep.R
import com.example.app_sisaep.view.navigation.Routes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.max

import androidx.compose.ui.res.stringResource

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CalendarioScreen(navController: NavController) {

    val navItems = listOf(
        BottomNavItem(stringResource(R.string.home)) { androidx.compose.material3.Icon(Icons.Filled.Home, null) },
        BottomNavItem(stringResource(R.string.calendar)) { androidx.compose.material3.Icon(Icons.Filled.CalendarMonth, null) },
        BottomNavItem(stringResource(R.string.agenda)) { androidx.compose.material3.Icon(Icons.Filled.Schedule, null) },
        BottomNavItem(stringResource(R.string.classes)) { androidx.compose.material3.Icon(Icons.Filled.School, null) },
    )

    AppScaffold(
        selectedIndex = 1,
        onItemSelected = { index ->
            when (index) {
                0 -> navController.navigate(Routes.Home)
                1 -> navController.navigate(Routes.Calendario)
                2 -> navController.navigate(Routes.Agenda)
                3 -> navController.navigate(Routes.Clases)
            }
        },
        onGenerateQrClick = { navController.navigate(Routes.GenerarQR) },
        onReadQrClick = { navController.navigate(Routes.ScanQR) },
        topBarLogoRes = R.drawable.ipn,
        onConfigClick = { navController.navigate(Routes.Config) },
        onLogoutClick = {
            navController.navigate(Routes.Login) {
                popUpTo(0)
                launchSingleTop = true
            }
        },
        navItems = navItems
    ) { innerPadding ->

        val context = LocalContext.current
        val density = LocalDensity.current

        var scale by remember { mutableFloatStateOf(1f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            // tamaño del contenedor en PX (para limitar offsets)
            val containerW = with(density) { maxWidth.toPx() }
            val containerH = with(density) { maxHeight.toPx() }

            fun clampOffsets(currentScale: Float, x: Float, y: Float): Pair<Float, Float> {
                // a scale=1 el contenido llena el contenedor (por ContentScale.Crop)
                val scaledW = containerW * currentScale
                val scaledH = containerH * currentScale

                val maxX = max(0f, (scaledW - containerW) / 2f)
                val maxY = max(0f, (scaledH - containerH) / 2f)

                return x.coerceIn(-maxX, maxX) to y.coerceIn(-maxY, maxY)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 5f)

                            val newX = offsetX + pan.x
                            val newY = offsetY + pan.y

                            val (cx, cy) = clampOffsets(newScale, newX, newY)

                            scale = newScale
                            offsetX = cx
                            offsetY = cy
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        // ✅ Opción A: desde drawable (tu app nueva)
                        .data(R.drawable.cal_escolarizado)

                        // ✅ Opción B: si lo quieres como antes desde assets, usa esta en lugar de la de arriba:
                        // .data("file:///android_asset/calendarioipn-escolarizada.jpg")

                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.school_calendar),
                    modifier = Modifier
                        .fillMaxSize() // ✅ ocupa toda la pantalla
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    contentScale = ContentScale.Crop, // ✅ llena pantalla como “visor”
                    placeholder = ColorPainter(Color.LightGray),
                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                )
            }
        }
    }
}
