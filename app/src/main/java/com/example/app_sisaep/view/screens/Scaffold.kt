package com.example.app_sisaep.view.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app_sisaep.R
import com.example.app_sisaep.viewModel.AuthApp
import com.example.app_sisaep.viewModel.RecordarSesion
import kotlinx.coroutines.launch

data class BottomNavItem(
    val label: String,
    val icon: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    onGenerateQrClick: () -> Unit,
    onReadQrClick: () -> Unit,
    nombreUsuario: String = "Sisaap",
    tituloTopBar: String = "Welcome to",
    versionApp: String = "1.0.0",
    onConfigClick: () -> Unit = {},
    onLogoutClick: () -> Unit,
    topBarLogoRes: Int? = null, // (ya no lo usamos porque ahora es logo_ipn_blanco)
    navItems: List<BottomNavItem>, // 4 items (izq 2, der 2)
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    require(navItems.size == 4) { "navItems debe tener exactamente 4 elementos." }

    // 🎨 IPN Theme
    val guindaIPN = Color(0xFF7A003C)
    val blanco = Color.White

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var qrMenuExpanded by remember { mutableStateOf(false) }

    // ✅ Insets reales del dispositivo (para que no se corte el menú flotante)
    val navBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .background(Color(0xFFF3F3F3))
                    .padding(16.dp)
            ) {
                Text("Menú", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch { drawerState.close() }
                        onConfigClick()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Configuración")
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        scope.launch {
                            qrMenuExpanded = false
                            drawerState.close()

                            // 1) Cierra sesión Supabase
                            AuthApp.logout()

                            // 2) Recordar sesión (solo supabase)
                            RecordarSesion.cerrarSesion()

                            // 3) Navegación la controla la pantalla
                            onLogoutClick()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.onError)
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Versión $versionApp",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    ) {
        Scaffold(
            // ✅ Respeta recortes, barra de estado, nav bar, etc.
            contentWindowInsets = WindowInsets.safeDrawing,

            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "$tituloTopBar, $nombreUsuario",
                            color = blanco,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú",
                                tint = blanco
                            )
                        }
                    },
                    actions = {
                        // ✅ Logo blanco fijo
                        Image(
                            painter = painterResource(id = R.drawable.logo_ipn_blanco),
                            contentDescription = "IPN",
                            modifier = Modifier
                                .size(34.dp)
                                .padding(end = 10.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = guindaIPN,
                        titleContentColor = blanco,
                        navigationIconContentColor = blanco,
                        actionIconContentColor = blanco
                    )
                )
            },

            bottomBar = {
                NavigationBar(

                    containerColor = guindaIPN
                ) {
                    @Composable
                    fun NavLabel(text: String) {
                        Text(
                            text = text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    // Item 0
                    NavigationBarItem(
                        icon = navItems[0].icon,
                        label = { NavLabel(navItems[0].label) },
                        alwaysShowLabel = false,
                        selected = selectedIndex == 0,
                        onClick = {
                            qrMenuExpanded = false
                            onItemSelected(0)
                        },
                        colors = navColors()
                    )

                    // Item 1
                    NavigationBarItem(
                        icon = navItems[1].icon,
                        label = { NavLabel(navItems[1].label) },
                        alwaysShowLabel = false,
                        selected = selectedIndex == 1,
                        onClick = {
                            qrMenuExpanded = false
                            onItemSelected(1)
                        },
                        colors = navColors()
                    )

                    // ✅ Botón central QR estilo FAB (guinda + icono blanco + elevación)
                    Box(
                        modifier = Modifier
                            .size(55.dp)
                            .offset(y = (-3).dp)
                            .shadow(10.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { qrMenuExpanded = !qrMenuExpanded }
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "QR",
                                tint = Color(0xFF7A003C),
                                modifier = Modifier.size(28.dp)
                            )

                        }
                    }

                    // Item 2
                    NavigationBarItem(
                        icon = navItems[2].icon,
                        label = { NavLabel(navItems[2].label) },
                        alwaysShowLabel = false,
                        selected = selectedIndex == 2,
                        onClick = {
                            qrMenuExpanded = false
                            onItemSelected(2)
                        },
                        colors = navColors()
                    )

                    // Item 3
                    NavigationBarItem(
                        icon = navItems[3].icon,
                        label = { NavLabel(navItems[3].label) },
                        alwaysShowLabel = false,
                        selected = selectedIndex == 3,
                        onClick = {
                            qrMenuExpanded = false
                            onItemSelected(3)
                        },
                        colors = navColors()
                    )
                }
            },

            floatingActionButton = floatingActionButton
        ) { paddingValues ->

            Box(modifier = Modifier.fillMaxSize()) {

                content(paddingValues)

                // Backdrop para cerrar menú QR tocando afuera
                if (qrMenuExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { qrMenuExpanded = false }
                    )
                }

                AnimatedVisibility(
                    visible = qrMenuExpanded,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        // ✅ dinámico según dispositivo
                        .padding(bottom = navBarsBottom + 92.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    qrMenuExpanded = false
                                    onGenerateQrClick()
                                },
                                modifier = Modifier.width(220.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF7A003C), // guinda IPN
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(10.dp))
                                Text("Generar QR")
                            }


                            OutlinedButton(
                                onClick = {
                                    qrMenuExpanded = false
                                    onReadQrClick()
                                },
                                modifier = Modifier.width(220.dp),
                                border = null,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(Icons.Default.CameraAlt, null)
                                Spacer(Modifier.width(10.dp))
                                Text("Escanear QR")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Color.White,
    selectedTextColor = Color.White,
    unselectedIconColor = Color.White.copy(alpha = 0.75f),
    unselectedTextColor = Color.White.copy(alpha = 0.75f),
    indicatorColor = Color.White.copy(alpha = 0.15f)
)
