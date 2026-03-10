package com.example.app_sisaep.viewModel



import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun esPersonalAdministrativo(supabase: SupabaseClient): Boolean {

    return withContext(Dispatchers.IO) {

        val user = supabase.auth.currentUserOrNull() ?: return@withContext false

        val result = supabase
            .from("personal_administrativo")
            .select {
                filter {
                    eq("id", user.id)
                }
            }

        result.data.isNotEmpty()
    }
}