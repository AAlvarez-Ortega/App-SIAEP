package com.example.app_sisaep.view.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    nombreUsuario: String = "",
    versionApp: String = "1.0.0",
    onConfigClick: () -> Unit = {},
    onLogoutClick: () -> Unit,
    onUserClick: () -> Unit,
    navItems: List<BottomNavItem>,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    require(navItems.size == 4) { "navItems debe tener exactamente 4 elementos." }

    val guindaIPN = Color(0xFF7A003C)
    val blanco = Color.White

    val drawerBackground = MaterialTheme.colorScheme.surface
    val drawerTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val scaffoldBackground = MaterialTheme.colorScheme.background
    val qrCenterButtonBackground = MaterialTheme.colorScheme.surface
    val qrCenterButtonIcon = guindaIPN

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var qrMenuExpanded by remember { mutableStateOf(false) }

    val noInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
    val navBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .background(drawerBackground)
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.menu),
                    style = MaterialTheme.typography.titleLarge,
                    color = drawerTextColor
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch { drawerState.close() }
                        onConfigClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = guindaIPN,
                        contentColor = blanco
                    )
                ) {
                    Text(stringResource(R.string.settings))
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        scope.launch {
                            qrMenuExpanded = false
                            drawerState.close()
                            AuthApp.logout()
                            RecordarSesion.cerrarSesion()
                            onLogoutClick()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.logout))
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.version_label, versionApp),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor
                )
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = noInsets,
            topBar = {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    windowInsets = noInsets,
                    title = {
                        Text(
                            text = stringResource(R.string.welcome_user, nombreUsuario),
                            color = blanco,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.menu),
                                tint = blanco
                            )
                        }
                    },
                    actions = {
                        UserAvatar(
                            nombre = nombreUsuario,
                            onClick = onUserClick,
                            modifier = Modifier.padding(end = 12.dp)
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
                Box(
                    modifier = Modifier.navigationBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    NavigationBar(
                        windowInsets = noInsets,
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

                        Spacer(modifier = Modifier.weight(1f))

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

                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .offset(y = (-6).dp)
                            .shadow(10.dp, CircleShape)
                            .clip(CircleShape)
                            .background(qrCenterButtonBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { qrMenuExpanded = !qrMenuExpanded }
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = stringResource(R.string.qr),
                                tint = qrCenterButtonIcon,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            },
            floatingActionButton = floatingActionButton
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scaffoldBackground)
                    .padding(paddingValues)
            ) {
                content(PaddingValues(0.dp))

                if (qrMenuExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                qrMenuExpanded = false
                            }
                    )
                }

                AnimatedVisibility(
                    visible = qrMenuExpanded,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
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
                                    containerColor = guindaIPN,
                                    contentColor = blanco
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(stringResource(R.string.generate_qr))
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
                                Text(stringResource(R.string.scan_qr))
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

@Composable
fun UserAvatar(
    nombre: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inicial = if (nombre.isNotEmpty()) nombre.take(1).uppercase() else "?"

    Surface(
        onClick = onClick,
        modifier = modifier.size(36.dp),
        shape = CircleShape,
        color = Color.White
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = inicial,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF7A003C),
                fontWeight = FontWeight.Bold
            )
        }
    }
}