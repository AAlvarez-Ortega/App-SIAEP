package com.example.app_sisaep.viewModel



import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.delay

object RecordarSesion {

    /**
     * Devuelve true si Supabase ya tiene una sesión cargada.
     */
    suspend fun haySesionActiva(): Boolean {
        return SupabaseConnectionApp.client.auth.currentSessionOrNull() != null
    }

    /**
     * Espera a que Supabase restaure la sesión desde storage (si existe).
     * Útil justo al abrir la app o antes de pantallas protegidas (QR).
     *
     * timeoutMs: cuánto esperamos máximo
     * tickMs: cada cuánto revisamos
     */
    suspend fun esperarSesion(timeoutMs: Long = 2500L, tickMs: Long = 150L): Boolean {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (SupabaseConnectionApp.client.auth.currentSessionOrNull() != null) return true
            delay(tickMs)
        }
        return SupabaseConnectionApp.client.auth.currentSessionOrNull() != null
    }

    /**
     * Cierra sesión REAL en Supabase.
     */
    suspend fun cerrarSesion() {
        SupabaseConnectionApp.client.auth.signOut()
    }
}
