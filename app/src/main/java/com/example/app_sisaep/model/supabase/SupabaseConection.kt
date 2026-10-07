package com.example.app_sisaep.model.supabase

/** Compatibilidad local: todos los flujos usan el proyecto principal operativo. */
object SupabaseConnection {
    val client get() = SupabaseConnectionApp.client
}
