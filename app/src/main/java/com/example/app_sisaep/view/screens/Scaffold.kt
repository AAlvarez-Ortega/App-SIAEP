package com.example.app_sisaep.view.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_sisaep.R
import com.example.app_sisaep.viewModel.AuthApp
import com.example.app_sisaep.viewModel.RecordarSesion
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Paleta institucional centralizada ─────────────────────────────────────────
private val GuindaOscuro   = Color(0xFF5C0011)
private val GuindaPrimario = Color(0xFF7A003C)
private val GuindaMedio    = Color(0xFF9B1B30)
private val GuindaSuave    = Color(0xFFF5E6ED)
private val Blanco         = Color.White

private val GradienteGuinda = Brush.horizontalGradient(
    listOf(GuindaOscuro, GuindaMedio)
)

// ── Data class ────────────────────────────────────────────────────────────────
data class BottomNavItem(
    val label: String,
    val icon: @Composable () -> Unit
)

// ─────────────────────────────────────────────────────────────────────────────

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

    val drawerState    = rememberDrawerState(DrawerValue.Closed)
    val scope          = rememberCoroutineScope()
    var qrMenuExpanded by remember { mutableStateOf(false) }

    val noInsets       = WindowInsets(0.dp)
    val navBarsBottom  = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Texto de bienvenida vivo: saludo según hora del día
    val saludoActual = remember {
        val hora = LocalTime.now().hour
        when {
            hora < 12 -> "Buenos días"
            hora < 19 -> "Buenas tardes"
            else      -> "Buenas noches"
        }
    }

    // Fecha formateada para el subtítulo del TopBar
    val fechaHoy = remember {
        val fmt = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "MX"))
        LocalDate.now().format(fmt).replaceFirstChar { it.uppercase() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            DrawerContenido(
                nombreUsuario  = nombreUsuario,
                versionApp     = versionApp,
                onConfigClick  = { scope.launch { drawerState.close() }; onConfigClick() },
                onLogoutClick  = {
                    scope.launch {
                        qrMenuExpanded = false
                        drawerState.close()
                        AuthApp.logout()
                        RecordarSesion.cerrarSesion()
                        onLogoutClick()
                    }
                },
                onUserClick    = { scope.launch { drawerState.close() }; onUserClick() }
            )
        }
    ) {
        Scaffold(
            contentWindowInsets = noInsets,
            topBar = {
                TopBarInstitucional(
                    saludo        = saludoActual,
                    nombreUsuario = nombreUsuario,
                    fechaHoy      = fechaHoy,
                    noInsets      = noInsets,
                    onMenuClick   = { scope.launch { drawerState.open() } },
                    onUserClick   = onUserClick
                )
            },
            bottomBar = {
                BottomBarConQr(
                    navItems       = navItems,
                    selectedIndex  = selectedIndex,
                    qrMenuExpanded = qrMenuExpanded,
                    noInsets       = noInsets,
                    onItemSelected = { idx -> qrMenuExpanded = false; onItemSelected(idx) },
                    onQrToggle     = { qrMenuExpanded = !qrMenuExpanded }
                )
            },
            floatingActionButton = floatingActionButton,
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                content(PaddingValues(0.dp))

                // Capa de cierre del menú QR al tocar fuera
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

                // Menú QR flotante
                AnimatedVisibility(
                    visible = qrMenuExpanded,
                    enter   = fadeIn(tween(200)) + slideInVertically(tween(220)) { it / 3 },
                    exit    = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 3 },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = navBarsBottom + 96.dp)
                ) {
                    MenuQr(
                        onGenerateClick = { qrMenuExpanded = false; onGenerateQrClick() },
                        onScanClick     = { qrMenuExpanded = false; onReadQrClick() }
                    )
                }
            }
        }
    }
}

// ── TopBar institucional con saludo + fecha ───────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarInstitucional(
    saludo: String,
    nombreUsuario: String,
    fechaHoy: String,
    noInsets: WindowInsets,
    onMenuClick: () -> Unit,
    onUserClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GradienteGuinda)
    ) {
        TopAppBar(
            modifier      = Modifier.statusBarsPadding(),
            windowInsets  = noInsets,
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        // "Buenos días, Juan" — saludo contextual
                        text     = if (nombreUsuario.isNotBlank()) "$saludo, ${nombreUsuario.substringBefore(" ")}" else saludo,
                        color    = Blanco,
                        style    = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text  = fechaHoy,
                        color = Blanco.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector     = Icons.Rounded.Menu,
                        contentDescription = stringResource(R.string.menu),
                        tint = Blanco
                    )
                }
            },
            actions = {
                UserAvatar(
                    nombre  = nombreUsuario,
                    onClick = onUserClick,
                    modifier = Modifier.padding(end = 12.dp)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor           = Color.Transparent,
                titleContentColor        = Blanco,
                navigationIconContentColor = Blanco,
                actionIconContentColor   = Blanco
            )
        )
    }
}

// ── BottomBar con botón QR central flotante ───────────────────────────────────
@Composable
private fun BottomBarConQr(
    navItems: List<BottomNavItem>,
    selectedIndex: Int,
    qrMenuExpanded: Boolean,
    noInsets: WindowInsets,
    onItemSelected: (Int) -> Unit,
    onQrToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GradienteGuinda)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        NavigationBar(
            windowInsets   = noInsets,
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            navItems.take(2).forEachIndexed { idx, item ->
                NavItemConIndicador(
                    item     = item,
                    selected = selectedIndex == idx,
                    onClick  = { onItemSelected(idx) }
                )
            }

            // Espacio central para el botón QR flotante
            Spacer(modifier = Modifier.weight(1f))

            navItems.drop(2).forEachIndexed { i, item ->
                val idx = i + 2
                NavItemConIndicador(
                    item     = item,
                    selected = selectedIndex == idx,
                    onClick  = { onItemSelected(idx) }
                )
            }
        }

        // Botón QR central — firma UX: anillo pulsante cuando está activo
        BotoQrCentral(
            expanded = qrMenuExpanded,
            onClick  = onQrToggle,
            modifier = Modifier.offset(y = (-8).dp)
        )
    }
}

// ── Botón QR con anillo pulsante ──────────────────────────────────────────────
@Composable
private fun BotoQrCentral(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Anillo pulsante infinito cuando está CERRADO (invita a la acción)
    val pulsoInfinito = rememberInfiniteTransition(label = "QrPulso")
    val escalaAnillo by pulsoInfinito.animateFloat(
        initialValue = 1f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AnilloPulso"
    )

    // Rotación del ícono al abrir/cerrar
    val rotacion by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "QrRotacion"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Anillo exterior pulsante (solo visible cuando está cerrado)
        if (!expanded) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .scale(escalaAnillo)
                    .clip(CircleShape)
                    .background(Blanco.copy(alpha = 0.18f))
            )
        }

        // Botón principal
        Box(
            modifier = Modifier
                .size(58.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector        = if (expanded) Icons.Rounded.Close else Icons.Rounded.QrCodeScanner,
                    contentDescription = stringResource(R.string.qr),
                    tint               = GuindaPrimario,
                    modifier           = Modifier
                        .size(28.dp)
                        .graphicsLayerRotation(rotacion)
                )
            }
        }
    }
}

// ── Item de nav con indicador de línea superior ───────────────────────────────
@Composable
private fun RowScope.NavItemConIndicador(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        icon = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Línea indicadora superior (más legible que el círculo sobre gradiente)
                AnimatedVisibility(visible = selected) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Blanco)
                            .padding(bottom = 2.dp)
                    )
                }
                item.icon()
            }
        },
        label = {
            Text(
                text     = item.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style    = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        alwaysShowLabel = false,
        selected = selected,
        onClick  = onClick,
        colors   = navColors()
    )
}

// ── Menú QR flotante con cards descriptivas ───────────────────────────────────
@Composable
private fun MenuQr(
    onGenerateClick: () -> Unit,
    onScanClick: () -> Unit
) {
    Surface(
        shape          = RoundedCornerShape(20.dp),
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        color          = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text  = "Asistencia",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )

            // Opción: Generar QR
            OpcionQr(
                icono       = Icons.Rounded.QrCode,
                titulo      = stringResource(R.string.generate_qr),
                descripcion = "Muestra tu código al maestro",
                onClick     = onGenerateClick,
                relleno     = true
            )

            // Opción: Leer QR
            OpcionQr(
                icono       = Icons.Rounded.CameraAlt,
                titulo      = stringResource(R.string.scan_qr),
                descripcion = "Escanea el código del salón",
                onClick     = onScanClick,
                relleno     = false
            )
        }
    }
}

@Composable
private fun OpcionQr(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    onClick: () -> Unit,
    relleno: Boolean
) {
    val bgColor      = if (relleno) GuindaPrimario else Color.Transparent
    val contentColor = if (relleno) Blanco else MaterialTheme.colorScheme.onSurface
    val borderMod    = if (!relleno) Modifier.background(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        RoundedCornerShape(14.dp)
    ) else Modifier

    Row(
        modifier = Modifier
            .width(240.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .then(borderMod)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector        = icono,
            contentDescription = null,
            tint               = contentColor,
            modifier           = Modifier.size(24.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text       = titulo,
                color      = contentColor,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text  = descripcion,
                color = contentColor.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ── Drawer lateral rediseñado ─────────────────────────────────────────────────
@Composable
private fun DrawerContenido(
    nombreUsuario: String,
    versionApp: String,
    onConfigClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onUserClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        // Header del drawer: gradiente + avatar grande
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GradienteGuinda)
                .clickable(onClick = onUserClick)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar grande
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Blanco),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = nombreUsuario.take(1).uppercase().ifEmpty { "?" },
                        style      = MaterialTheme.typography.headlineSmall,
                        color      = GuindaPrimario,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text       = nombreUsuario.ifBlank { "Estudiante" },
                        style      = MaterialTheme.typography.titleMedium,
                        color      = Blanco,
                        fontWeight = FontWeight.Bold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Text(
                        text  = "Ver mi perfil →",
                        style = MaterialTheme.typography.bodySmall,
                        color = Blanco.copy(alpha = 0.75f)
                    )
                }
            }
        }

        // Cuerpo del drawer
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                text     = "Opciones",
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            ItemDrawer(
                icono   = Icons.Rounded.Settings,
                texto   = stringResource(R.string.settings),
                onClick = onConfigClick
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Cerrar sesión
            ItemDrawer(
                icono   = Icons.Rounded.Logout,
                texto   = stringResource(R.string.logout),
                onClick = onLogoutClick,
                esDestructivo = true
            )
        }

        // Versión al pie
        Text(
            text     = stringResource(R.string.version_label, versionApp),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ItemDrawer(
    icono: ImageVector,
    texto: String,
    onClick: () -> Unit,
    esDestructivo: Boolean = false
) {
    val color = if (esDestructivo) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (esDestructivo) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector        = icono,
            contentDescription = null,
            tint               = color,
            modifier           = Modifier.size(20.dp)
        )
        Text(
            text       = texto,
            style      = MaterialTheme.typography.bodyMedium,
            color      = color,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Extensión interna para aplicar rotación vía graphicsLayer ─────────────────
private fun Modifier.graphicsLayerRotation(degrees: Float): Modifier =
    this.then(Modifier.graphicsLayer { rotationZ = degrees })

// ── Colores compartidos de NavigationBarItem ──────────────────────────────────
@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor     = Blanco,
    selectedTextColor     = Blanco,
    unselectedIconColor   = Blanco.copy(alpha = 0.65f),
    unselectedTextColor   = Blanco.copy(alpha = 0.65f),
    indicatorColor        = Color.Transparent   // quitamos el oval genérico; usamos la línea propia
)

// ── UserAvatar (público para uso en otras pantallas) ──────────────────────────
@Composable
fun UserAvatar(
    nombre: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick   = onClick,
        modifier  = modifier.size(36.dp),
        shape     = CircleShape,
        color     = Blanco,
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text       = nombre.take(1).uppercase().ifEmpty { "?" },
                style      = MaterialTheme.typography.titleMedium,
                color      = GuindaPrimario,
                fontWeight = FontWeight.Bold
            )
        }
    }
}