package com.example.app_sisaep.viewModel

import com.example.app_sisaep.model.dto.AvisoGlobal
import com.example.app_sisaep.model.dto.EscuelaDto
import com.example.app_sisaep.model.dto.SolicitudIdDto
import com.example.app_sisaep.model.dto.SolicitudInsertDto
import com.example.app_sisaep.model.supabase.SupabaseConnection
import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
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
     * ✅ CAMBIO: antes numeroBoleta + columna numero_boleta
     * Ahora: boletaOEmpleado + columna boleta_o_empleado
     *
     * Nota: tu lógica actual usa OR (si coincide boleta/empleado O coincide curp).
     * Eso replica tu comportamiento previo.
     */
    suspend fun existeSolicitud(boletaOEmpleado: String, curp: String): Boolean {
        val result = SupabaseConnection.client
            .from("solicitudes")
            .select {
                filter {
                    or {
                        eq("boleta_o_empleado", boletaOEmpleado)
                        eq("curp", curp)
                    }
                }
                limit(1)
            }
            .decodeList<SolicitudIdDto>() // solo necesitamos el id

        return result.isNotEmpty()
    }

    suspend fun obtenerEstadoSolicitudPorId(id: String): String? {
        val client = SupabaseConnection.client

        val result = client
            .from("solicitudes")
            .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("estado")) {
                filter { eq("id", id) }
                limit(1)
            }
            .decodeList<Map<String, String?>>()

        return result.firstOrNull()?.get("estado")
    }


    suspend fun obtenerAvisosActivos(): List<AvisoGlobal> {
        return SupabaseConnectionApp.client
            .from("avisos_globales")
            .select {
                filter {
                    eq("estado", "activo")
                }
            }
            .decodeList<AvisoGlobal>()
    }
}
