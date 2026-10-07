package com.example.app_sisaep.viewModel

import android.content.Context
import androidx.core.content.edit
import com.example.app_sisaep.model.dto.ComprobanteSolicitud

object estatus {

    private const val PREFS = "sisaep_prefs"
    // Se conserva el identificador antiguo de SISAP; no se mezcla con App-SISAEP.
    private const val KEY_PENDING_SOLICITUD_ID = "pending_solicitud_id_v2"
    private const val KEY_PENDING_SOLICITUD_TOKEN = "pending_solicitud_token_v2"

    enum class EstadoSolicitud { ACEPTADO, PENDIENTE, RECHAZADO, NO_EXISTE }

    fun guardarSolicitudPendiente(context: Context, comprobante: ComprobanteSolicitud) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            putString(KEY_PENDING_SOLICITUD_ID, comprobante.id)
            putString(KEY_PENDING_SOLICITUD_TOKEN, comprobante.token)
        }
    }

    fun limpiarSolicitudPendiente(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            remove(KEY_PENDING_SOLICITUD_ID)
            remove(KEY_PENDING_SOLICITUD_TOKEN)
        }
    }

    fun obtenerSolicitudPendienteId(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PENDING_SOLICITUD_ID, null)
    }

    fun obtenerComprobante(context: Context): ComprobanteSolicitud? {
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val id = preferences.getString(KEY_PENDING_SOLICITUD_ID, null) ?: return null
        val token = preferences.getString(KEY_PENDING_SOLICITUD_TOKEN, null) ?: return null
        return ComprobanteSolicitud(id, token)
    }

    /**
     * Retorna el estado real de la solicitud:
     * - ACEPTADO: desbloquea
     * - PENDIENTE: bloquea
     * - RECHAZADO: bloquea y muestra aviso
     * - NO_EXISTE: limpia el id guardado y desbloquea
     */
    suspend fun obtenerEstadoSolicitud(context: Context): EstadoSolicitud {
        val comprobante = obtenerComprobante(context) ?: return EstadoSolicitud.NO_EXISTE
        val estadoDb = RegistroApp.estado(comprobante)

        return when (estadoDb?.trim()?.lowercase()) {
            "aceptado" -> EstadoSolicitud.ACEPTADO
            "pendiente" -> EstadoSolicitud.PENDIENTE
            "rechazado" -> EstadoSolicitud.RECHAZADO
            null -> {
                limpiarSolicitudPendiente(context) // ya no existe / no se encontró
                EstadoSolicitud.NO_EXISTE
            }
            else -> {
                // si llega algo raro, tratamos como pendiente para evitar desbloqueos
                EstadoSolicitud.PENDIENTE
            }
        }
    }
}
