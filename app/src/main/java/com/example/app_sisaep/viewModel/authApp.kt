package com.example.app_sisaep.viewModel

import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession

object AuthApp {

    /**
     * Inicia sesión con email + password.
     * Nota: signInWith(...) retorna Unit en supabase-kt, por eso leemos currentSessionOrNull().
     */
    suspend fun login(userEmail: String, userPassword: String): Result<UserSession> {
        return try {
            val email = userEmail.trim().lowercase()
            val password = userPassword

            SupabaseConnectionApp.client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val session = SupabaseConnectionApp.client.auth.currentSessionOrNull()
                ?: throw IllegalStateException(
                    "No se pudo obtener sesión. Revisa credenciales o confirmación de correo."
                )

            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cierra sesión.
     */
    suspend fun logout(): Result<Unit> {
        return try {
            SupabaseConnectionApp.client.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Devuelve el usuario actual si hay sesión válida (o null si no).
     * Útil para mostrar perfil, UID, email, etc.
     */
    suspend fun getCurrentUser(): Result<UserInfo?> {
        return try {
            val user = SupabaseConnectionApp.client.auth.currentUserOrNull()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * (Opcional) Si quieres saber rápido si hay sesión.
     * OJO: puede existir sesión en storage pero estar expirada; si quieres confirmar,
     * llama requireSession() o getCurrentUser().
     */
    fun isLoggedIn(): Boolean {
        return SupabaseConnectionApp.client.auth.currentSessionOrNull() != null
    }

    /**
     * Lanza error si no existe sesión, y te regresa la sesión si sí existe.
     * Útil para proteger pantallas.
     */
    fun requireSession(): UserSession {
        return SupabaseConnectionApp.client.auth.currentSessionOrNull()
            ?: throw IllegalStateException("Sesión no encontrada. Inicia sesión nuevamente.")
    }

    /**
     * Envia correo para restablecer contraseña.
     * IMPORTANTE: en Supabase debes tener configurado el redirect de recovery en Auth settings.
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            val clean = email.trim().lowercase()
            SupabaseConnectionApp.client.auth.resetPasswordForEmail(clean)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
