package com.example.app_sisaep.viewModel

import android.content.Context
import androidx.core.content.edit

object estatus {

    private const val PREFS = "sisaep_prefs"
    private const val KEY_PENDING_SOLICITUD_ID = "pending_solicitud_id"

    enum class EstadoSolicitud { ACEPTADO, PENDIENTE, RECHAZADO, NO_EXISTE }

    fun guardarSolicitudPendiente(context: Context, solicitudId: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            putString(KEY_PENDING_SOLICITUD_ID, solicitudId)
        }
    }

    fun limpiarSolicitudPendiente(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            remove(KEY_PENDING_SOLICITUD_ID)
        }
    }

    fun obtenerSolicitudPendienteId(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PENDING_SOLICITUD_ID, null)
    }

    /**
     * Retorna el estado real de la solicitud:
     * - ACEPTADO: desbloquea
     * - PENDIENTE: bloquea
     * - RECHAZADO: bloquea y muestra aviso
     * - NO_EXISTE: limpia el id guardado y desbloquea
     */
    suspend fun obtenerEstadoSolicitud(context: Context): EstadoSolicitud {
        val id = obtenerSolicitudPendienteId(context) ?: return EstadoSolicitud.NO_EXISTE

        // Debes implementar esta función en consultaas (abajo te dejo el código)
        val estadoDb = consultaas.obtenerEstadoSolicitudPorId(id)

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