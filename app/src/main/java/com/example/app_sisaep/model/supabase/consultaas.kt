package com.example.app_sisaep.model.supabase


import com.example.app_sisaep.model.dto.EscuelaDto
import com.example.app_sisaep.model.dto.SolicitudIdDto
import com.example.app_sisaep.model.dto.SolicitudInsertDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

object consultaas {

    suspend fun getEscuelas(): List<EscuelaDto> {
        val client = SupabaseConnection.client
        return client
            .from("escuelas")
            .select {
                order("nombre", Order.ASCENDING)
            }
            .decodeList<EscuelaDto>()
    }

    /**
     * Inserta una solicitud y regresa el UUID generado.
     */
    suspend fun insertarSolicitud(payload: SolicitudInsertDto): String {
        val client = SupabaseConnection.client

        // Insert + returning id
        val inserted = client
            .from("solicitudes")
            .insert(payload) {
                select() // returning *
            }
            .decodeSingle<SolicitudIdDto>()

        return inserted.id
    }

    /**
     * Valida si existe la solicitud por id (para estatus "en proceso")
     */
    suspend fun existeSolicitudPorId(id: String): Boolean {
        val client = SupabaseConnection.client

        val res = client
            .from("solicitudes")
            .select {
                filter { eq("id", id) }
                limit(1)
            }
            .decodeList<SolicitudIdDto>()

        return res.isNotEmpty()
    }
}
