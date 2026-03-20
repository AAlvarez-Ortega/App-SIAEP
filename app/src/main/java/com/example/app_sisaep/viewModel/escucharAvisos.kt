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

/**
 * Función que crea un flujo (Flow) de avisos urgentes en tiempo real.
 */
suspend fun escucharAvisosUrgentes(): Flow<AvisoGlobal> {
    // 1. Creamos el canal de Realtime
    val canal = SupabaseConnectionApp.client.realtime.channel("avisos_urgentes")

    // 2. Configuramos el filtro para escuchar solo INSERTs en la tabla avisos_globales
    val flujoCambios = canal.postgresChangeFlow<PostgresAction.Insert>(
        schema = "public"
    ) {
        table = "avisos_globales"
    }

    // 3. Nos suscribimos al canal
    canal.subscribe()

    // 4. Transformamos el flujo para obtener solo avisos que sean "urgentes"
    return flujoCambios.mapNotNull { accion ->
        val aviso = accion.decodeRecord<AvisoGlobal>()
        if (aviso.tipo_aviso?.lowercase() == "urgente" && aviso.estado == "activo") {
            aviso
        } else {
            null
        }
    }
}

// En escucharAvisos.kt

// ... tus imports anteriores

// En escucharAvisos.kt

suspend fun obtenerUltimoAvisoUrgente(): AvisoGlobal? {
    return try {
        val cliente = SupabaseConnectionApp.client

        // Buscamos en la tabla avisos_globales
        val resultado = cliente.from("avisos_globales")
            .select {
                filter {
                    eq("tipo_aviso", "urgente")
                    eq("estado", "activo")
                }
                // Ordenamos por el más reciente
                order("creado_en", order = Order.DESCENDING)
                limit(1)
            }.decodeSingleOrNull<AvisoGlobal>()

        resultado
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}