package com.example.app_sisaep

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.app_sisaep.model.dto.AvisoGlobal
import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import com.example.app_sisaep.ui.theme.AppSisaepTheme
import com.example.app_sisaep.view.navigation.AppNavHost
import com.example.app_sisaep.view.screens.Btncreateavisos.PantallaCompletaAviso
import com.example.app_sisaep.view.screens.getDarkModePreference
import com.example.app_sisaep.viewModel.escucharAvisosUrgentes
import com.example.app_sisaep.viewModel.obtenerUltimoAvisoUrgente
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()

            var darkMode by remember { mutableStateOf(getDarkModePreference(context)) }
            var avisoUrgente by remember { mutableStateOf<AvisoGlobal?>(null) }

            val sharedPrefs = remember {
                context.getSharedPreferences("avisos_prefs", Context.MODE_PRIVATE)
            }

            // ── Aviso histórico al autenticarse ───────────────────────────────
            LaunchedEffect(Unit) {
                SupabaseConnectionApp.client.auth.sessionStatus.collectLatest { estatus ->
                    if (estatus is SessionStatus.Authenticated) {
                        try {
                            val avisoExistente = obtenerUltimoAvisoUrgente()
                            if (avisoExistente != null) {
                                val ultimoIdVisto = sharedPrefs.getString("ultimo_aviso_id", "")
                                if (avisoExistente.id != ultimoIdVisto) {
                                    avisoUrgente = avisoExistente
                                }
                            }
                        } catch (_: HttpRequestException) {
                            // Sin conexión — la app sigue funcionando sin el aviso
                        } catch (_: Exception) {
                            // Cualquier otro error de red o Supabase
                        }
                    }
                }
            }

            // ── Escucha en tiempo real de avisos urgentes ─────────────────────
            LaunchedEffect(Unit) {
                launch {
                    escucharAvisosUrgentes()
                        .catch { /* Error en el canal realtime — ignoramos silenciosamente */ }
                        .collect { nuevoAviso ->
                            val ultimoIdVisto = sharedPrefs.getString("ultimo_aviso_id", "")
                            if (nuevoAviso.id != ultimoIdVisto) {
                                avisoUrgente = nuevoAviso
                            }
                        }
                }
            }

            AppSisaepTheme(darkTheme = darkMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavHost(
                            navController = navController,
                            darkMode = darkMode,
                            onDarkModeChange = { darkMode = it }
                        )
                    }

                    avisoUrgente?.let { aviso ->
                        PantallaCompletaAviso(
                            aviso = aviso,
                            onConfirm = {
                                sharedPrefs.edit()
                                    .putString("ultimo_aviso_id", aviso.id)
                                    .apply()
                                avisoUrgente = null
                            }
                        )
                    }
                }
            }
        }
    }
}