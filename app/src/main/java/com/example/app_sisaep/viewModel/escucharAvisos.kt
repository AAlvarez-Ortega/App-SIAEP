package com.example.app_sisaep.viewModel

import com.example.app_sisaep.model.dto.AvisoGlobal
import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

suspend fun escucharAvisosUrgentes(): Flow<AvisoGlobal> {
    val canal = SupabaseConnectionApp.client.realtime.channel("avisos_urgentes")

    val flujoCambios = canal.postgresChangeFlow<PostgresAction.Insert>(
        schema = "public"
    ) {
        table = "avisos_globales"
    }

    canal.subscribe()

    return flujoCambios.mapNotNull { accion ->
        try {
            val aviso = accion.decodeRecord<AvisoGlobal>()
            // Simplificado para evitar nulos inesperados en el backend
            if (aviso.tipo_aviso?.lowercase() == "urgente" && aviso.estado?.lowercase() == "activo") {
                aviso
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

suspend fun obtenerUltimoAvisoUrgente(): AvisoGlobal? {
    return try {
        val cliente = SupabaseConnectionApp.client

        // Buscamos en la tabla avisos_globales ordenando por el ID o por fecha más reciente
        cliente.from("avisos_globales")
            .select {
                filter {
                    eq("tipo_aviso", "urgente")
                    eq("estado", "activo")
                }
                order("creado_en", order = Order.DESCENDING)
                limit(1)
            }.decodeSingleOrNull<AvisoGlobal>()
    } catch (e: Exception) {
        println("Error buscando avisos urgentes históricos: ${e.message}")
        e.printStackTrace()
        null
    }
}