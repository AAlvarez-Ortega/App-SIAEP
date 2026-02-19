package com.example.app_sisaep.model.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.okhttp.OkHttp


object SupabaseConnectionApp {

    private const val SUPABASE_URL = "https://wdgsvdjojwjebjrpgopn.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_TxHT2AsKDXlxYRGh0VgRMw_5VozTQ_p"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            httpEngine = OkHttp.create()

            install(Postgrest)

            install(Auth) {
                autoLoadFromStorage = true
                alwaysAutoRefresh = true
            }
        }
    }
}
