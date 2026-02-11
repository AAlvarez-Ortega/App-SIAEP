package com.example.app_sisaep.viewModel

import android.content.Context
import androidx.core.content.edit
import com.example.app_sisaep.viewModel.consultaas

object estatus {

    private const val PREFS = "sisaep_prefs"
    private const val KEY_PENDING_SOLICITUD_ID = "pending_solicitud_id"

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
     * Retorna true si hay solicitud pendiente y todavía existe en la tabla.
     */
    suspend fun haySolicitudEnProceso(context: Context): Boolean {
        val id = obtenerSolicitudPendienteId(context) ?: return false
        val existe = consultaas.existeSolicitudPorId(id)
        if (!existe) limpiarSolicitudPendiente(context) // ya no existe, limpio
        return existe
    }
}