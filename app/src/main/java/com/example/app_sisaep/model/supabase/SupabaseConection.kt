package com.example.app_sisaep.model.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseConnection {

    private const val SUPABASE_URL = "https://acxfppvfzjihgkvvljoq.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_8rTJbNVD_MdzSDZj6nS7ig_bDFK6s2a"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }
}
