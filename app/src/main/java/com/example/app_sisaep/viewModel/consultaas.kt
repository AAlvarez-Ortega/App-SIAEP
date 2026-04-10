package com.example.app_sisaep.viewModel

import com.example.app_sisaep.model.dto.AvisoGlobal
import com.example.app_sisaep.model.dto.ChatPreviewDto
import com.example.app_sisaep.model.dto.ConversacionDto
import com.example.app_sisaep.model.dto.EscuelaDto
import com.example.app_sisaep.model.dto.MensajeDto
import com.example.app_sisaep.model.dto.SolicitudIdDto
import com.example.app_sisaep.model.dto.SolicitudInsertDto
import com.example.app_sisaep.model.dto.UsuarioDto
import com.example.app_sisaep.model.supabase.SupabaseConnection
import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
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

    suspend fun obtenerMisDatos(): UsuarioDto? {
        return try {
            val userId = SupabaseConnectionApp.client.auth.currentUserOrNull()?.id ?: return null
            SupabaseConnectionApp.client
                .from("usuarios")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<UsuarioDto>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    suspend fun obtenerContactosPorEscuela(escuelaCct: String): List<UsuarioDto> {
        return try {
            val miId = SupabaseConnectionApp.client.auth.currentUserOrNull()?.id
            SupabaseConnectionApp.client
                .from("usuarios")
                .select {
                    filter {
                        eq("escuela_cct", escuelaCct)
                        // Opcional: No mostrarte a ti mismo en la lista de contactos
                        if (miId != null) neq("id", miId)
                    }
                    order("nombre", Order.ASCENDING)
                }
                .decodeList<UsuarioDto>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // En el objeto consultaas dentro de consultas.kt

    /**
     * Busca una conversación existente entre dos usuarios o crea una nueva.
     */
    suspend fun obtenerOCrearConversacion(usuario1: String, usuario2: String): String? {
        return try {
            val client = SupabaseConnectionApp.client

            // Intentamos buscar si ya existe (en cualquier orden de usuario_1 y usuario_2)
            val existe = client.from("conversaciones").select {
                filter {
                    or {
                        and { eq("usuario_1", usuario1); eq("usuario_2", usuario2) }
                        and { eq("usuario_1", usuario2); eq("usuario_2", usuario1) }
                    }
                }
                limit(1)
            }.decodeSingleOrNull<ConversacionDto>()

            if (existe != null) return existe.id

            // Si no existe, la creamos
            val nueva = client.from("conversaciones").insert(
                mapOf("usuario_1" to usuario1, "usuario_2" to usuario2)
            ) { select() }.decodeSingle<ConversacionDto>()

            nueva.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Inserta un mensaje en la base de datos.
     */
    suspend fun enviarMensaje(mensaje: MensajeDto): Boolean {
        return try {
            SupabaseConnectionApp.client.from("mensajes").insert(mensaje)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Obtiene el historial de mensajes de una conversación específica.
     */
    suspend fun obtenerMensajes(conversacionId: String): List<MensajeDto> {
        return try {
            SupabaseConnectionApp.client.from("mensajes")
                .select {
                    filter { eq("conversacion_id", conversacionId) }
                    order("creado_en", Order.ASCENDING)
                }.decodeList<MensajeDto>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene la lista de usuarios con los que el usuario actual ya tiene una conversación.
     * Esto es para la pantalla principal de "Mensajes".
     */
    suspend fun obtenerMisChatsActivosOrdenados(): List<ChatPreviewDto> {
        return try {
            val miId = SupabaseConnectionApp.client.auth.currentUserOrNull()?.id ?: return emptyList()

            // 1. Traer conversaciones sin filtros pesados primero para evitar el UnknownRestException
            val todasLasConvs = SupabaseConnectionApp.client
                .from("conversaciones")
                .select().decodeList<ConversacionDto>()

            // 2. Filtramos en memoria de Kotlin (es más seguro si el RLS da problemas)
            val misConvs = todasLasConvs.filter { it.usuario_1 == miId || it.usuario_2 == miId }
                .sortedByDescending { it.updated_at ?: it.creado_en }

            val listaPreview = mutableListOf<ChatPreviewDto>()

            for (conv in misConvs) {
                val otroId = if (conv.usuario_1 == miId) conv.usuario_2 else conv.usuario_1

                // Traer último mensaje
                val ultimoMsj = try {
                    SupabaseConnectionApp.client.from("mensajes").select {
                        filter { eq("conversacion_id", conv.id) }
                        order("creado_en", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                        limit(1)
                    }.decodeSingleOrNull<MensajeDto>()
                } catch (e: Exception) { null }

                // Traer datos del otro usuario
                val usuario = try {
                    SupabaseConnectionApp.client.from("usuarios").select {
                        filter { eq("id", otroId) }
                    }.decodeSingleOrNull<UsuarioDto>()
                } catch (e: Exception) { null }

                if (usuario != null) {
                    listaPreview.add(ChatPreviewDto(
                        usuarioId = usuario.id,
                        nombreCompleto = "${usuario.nombre} ${usuario.apellido_paterno}",
                        ultimoMensaje = ultimoMsj?.contenido ?: "Sin mensajes aún",
                        fechaUltimoMensaje = conv.updated_at ?: conv.creado_en ?: ""
                    ))
                }
            }
            listaPreview
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

}
