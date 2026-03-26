package com.example.app_sisaep.view.screens.avsos

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.app_sisaep.model.dto.AvisoGlobal
import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import com.example.app_sisaep.viewModel.AvisosStorage
import com.example.app_sisaep.viewModel.consultaas.obtenerAvisosActivos
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import androidx.compose.ui.res.stringResource
import com.example.app_sisaep.R

@Composable
fun AvisosGlobalesListener() {

    val context = LocalContext.current

    var avisoActual by remember { mutableStateOf<AvisoGlobal?>(null) }

    val avisosVistosSesion = remember { mutableSetOf<String>() }

    LaunchedEffect(true) {
        try {
            val avisos = obtenerAvisosActivos()
            val avisoNoVisto = avisos.firstOrNull { !AvisosStorage.fueVisto(context, it.id.toString()) }
            avisoNoVisto?.let {
                avisoActual = it
                avisosVistosSesion.add(it.id.toString()) // 👈 marcar en sesión
            }
        } catch (_: Exception) {}

        val channel = SupabaseConnectionApp.client.realtime.channel("avisos_globales")

        channel.subscribe()

        channel.postgresChangeFlow<PostgresAction.Insert>(
            schema = "public"
        ) {
            table = "avisos_globales"
        }
            .collect { change: PostgresAction.Insert ->

                val aviso: AvisoGlobal = change.decodeRecord()

                val avisoId = aviso.id.toString()

                if (
                    aviso.estado == "activo" &&
                    !AvisosStorage.fueVisto(context, avisoId) &&
                    !avisosVistosSesion.contains(avisoId)
                ) {

                    avisoActual = aviso

                }

            }

    }

    avisoActual?.let { aviso ->

        AlertDialog(

            onDismissRequest = {},

            title = {
                Text(aviso.titulo)
            },

            text = {
                Text(aviso.mensaje)
            },

            confirmButton = {

                Button(

                    onClick = {

                        val avisoId = aviso.id.toString()

                        AvisosStorage.marcarVisto(
                            context,
                            avisoId
                        )

                        avisosVistosSesion.add(avisoId)

                        avisoActual = null

                    }

                ) {

                    Text(stringResource(R.string.understood_button))

                }

            }

        )

    }

}