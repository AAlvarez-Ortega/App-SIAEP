package com.example.app_sisaep.viewModel



import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from

suspend fun esPersonalAdministrativo(client: SupabaseClient): Boolean {

    val user = client.auth.currentUserOrNull() ?: return false

    val resultado = client
        .from("personal_administrativo")
        .select {
            filter {
                eq("id", user.id)
            }
        }
        .decodeList<Map<String, Any>>()

    return resultado.isNotEmpty()
}