package com.example.app_sisaep.viewModel

import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from

suspend fun crearAviso(
    titulo: String,
    mensaje: String,
    tipo: String,
    fechaExpiracion: String?
): Boolean {

    return try {

        val userId = SupabaseConnectionApp
            .client
            .auth
            .currentUserOrNull()
            ?.id ?: return false

        SupabaseConnectionApp.client
            .from("avisos_globales")
            .insert(
                mapOf(
                    "titulo" to titulo,
                    "mensaje" to mensaje,
                    "tipo_aviso" to tipo.lowercase(),
                    "creado_por" to userId,
                    "fecha_expiracion" to fechaExpiracion
                )
            )

        true

    } catch (e: Exception) {

        e.printStackTrace()
        false

    }

}