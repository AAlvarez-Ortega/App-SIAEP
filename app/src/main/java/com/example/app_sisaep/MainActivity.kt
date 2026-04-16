package com.example.app_sisaep

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.app_sisaep.model.dto.AvisoGlobal
import com.example.app_sisaep.ui.theme.AppSisaepTheme
import com.example.app_sisaep.view.navigation.AppNavHost
import com.example.app_sisaep.view.screens.Btncreateavisos.PantallaCompletaAviso
import com.example.app_sisaep.viewModel.escucharAvisosUrgentes
import com.example.app_sisaep.viewModel.obtenerUltimoAvisoUrgente
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_sisaep.viewModel.consultaas.obtenerMisDatos


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()
            var darkMode by remember { mutableStateOf(false) }

            // 1. Estado para el aviso urgente
            var avisoUrgente by remember { mutableStateOf<AvisoGlobal?>(null) }

            // 2. Acceso a SharedPreferences para recordar avisos vistos
            val sharedPrefs = remember {
                context.getSharedPreferences("avisos_prefs", Context.MODE_PRIVATE)
            }

            LaunchedEffect(Unit) {
                // Paso A: Consultar aviso existente al abrir
                launch {
                    val avisoExistente = obtenerUltimoAvisoUrgente()
                    if (avisoExistente != null) {
                        val ultimoIdVisto = sharedPrefs.getString("ultimo_aviso_id", "")
                        // Solo lo mostramos si el ID es diferente al que ya guardamos como "visto"
                        if (avisoExistente.id != ultimoIdVisto) {
                            avisoUrgente = avisoExistente
                        }
                    }
                }

                // Paso B: Escucha en tiempo real (Realtime)
                launch {
                    escucharAvisosUrgentes().collect { nuevoAviso ->
                        // Cuando llega uno nuevo "en vivo", siempre lo mostramos
                        avisoUrgente = nuevoAviso
                    }
                }
            }

            AppSisaepTheme(darkTheme = darkMode) {
                Box(modifier = Modifier.fillMaxSize()) {

                    Surface(modifier = Modifier.fillMaxSize()) {
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
                                // 3. Al dar OK, guardamos el ID en el teléfono para no repetir
                                sharedPrefs.edit().putString("ultimo_aviso_id", aviso.id).apply()
                                avisoUrgente = null
                            }
                        )
                    }
                }
            }
        }
    }
}

class MainViewModel : ViewModel() {
    var nombreUsuario by mutableStateOf("Cargando...")
        private set

    fun cargarDatosUsuario() {
        viewModelScope.launch {
            val datos = obtenerMisDatos()
            if (datos != null) {
                nombreUsuario = datos.nombre
            } else {
                nombreUsuario = "Invitado"
            }
        }
    }
}