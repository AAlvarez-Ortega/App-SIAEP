package com.example.app_sisaep.viewModel

import android.util.Log
import com.example.app_sisaep.model.dto.AvisoGlobal
import com.example.app_sisaep.model.dto.ChatPreviewDto
import com.example.app_sisaep.model.dto.ConversacionDto
import com.example.app_sisaep.model.dto.ContactoDto
import com.example.app_sisaep.model.dto.DiaEscolarDto
import com.example.app_sisaep.model.dto.EscuelaDto
import com.example.app_sisaep.model.dto.EventoIdUsuarioDto
import com.example.app_sisaep.model.dto.EventoInsertDto
import com.example.app_sisaep.model.dto.HoraClaseDto
import com.example.app_sisaep.model.dto.MensajeDto
import com.example.app_sisaep.model.dto.ComprobanteSolicitud
import com.example.app_sisaep.model.dto.SolicitudInsertDto
import com.example.app_sisaep.model.dto.UsuarioDto
import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


object consultaas {
    suspend fun getEscuelas(): List<EscuelaDto> {
        return RegistroApp.escuelas()
    }

    suspend fun insertarSolicitud(payload: SolicitudInsertDto, comprobante: ComprobanteSolicitud): String {
        return RegistroApp.enviar(payload, comprobante)
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
                .from("sic_usuarios")
                .select {
                    filter { eq("id_usuario", userId) }
                }
                .decodeSingleOrNull<UsuarioDto>()
        } catch (e: Exception) {
            println("Error obteniendo datos de Supabase: ${e.message}")
            null
        }
    }

    suspend fun obtenerContactos(): List<ContactoDto> {
        return try {
            SupabaseConnectionApp.client
                .postgrest.rpc("sisaep_contactos")
                .decodeList<ContactoDto>()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Busca una conversación existente entre dos usuarios o crea una nueva.
     */
    suspend fun obtenerOCrearConversacion(usuario1: String, usuario2: String): String? {
        return try {
            val client = SupabaseConnectionApp.client

            if (client.auth.currentUserOrNull()?.id != usuario1) return null
            // SQL identifica al remitente, valida el contacto y evita parejas duplicadas.
            client.postgrest.rpc("sisaep_conversacion", buildJsonObject {
                put("p_otro", usuario2)
            }).decodeAs<String>()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
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
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene la lista de usuarios con los que el usuario actual ya tiene una conversación.
     * Esto es para la pantalla principal de "Mensajes".
     */
    suspend fun obtenerMisChatsActivosOrdenados(): List<ChatPreviewDto> {
        return try {
            val miId =
                SupabaseConnectionApp.client.auth.currentUserOrNull()?.id ?: return emptyList()

            // RLS devuelve únicamente conversaciones de las que somos participantes.
            val todasLasConvs = SupabaseConnectionApp.client
                .from("conversaciones")
                .select().decodeList<ConversacionDto>()

            // El filtro local conserva la selección al construir la vista previa.
            val misConvs = todasLasConvs.filter { it.usuario_1 == miId || it.usuario_2 == miId }
                .sortedByDescending { it.updated_at ?: it.creado_en }

            val listaPreview = mutableListOf<ChatPreviewDto>()

            for (conv in misConvs) {
                val otroId = if (conv.usuario_1 == miId) conv.usuario_2 else conv.usuario_1

                // Traer último mensaje
                val ultimoMsj = try {
                    SupabaseConnectionApp.client.from("mensajes").select {
                        filter { eq("conversacion_id", conv.id) }
                        order("creado_en", order = Order.DESCENDING)
                        limit(1)
                    }.decodeSingleOrNull<MensajeDto>()
                } catch (_: Exception) {
                    null
                }

                // Traer datos del otro usuario
                val usuario = try {
                    SupabaseConnectionApp.client.postgrest.rpc("sisaep_contactos", buildJsonObject {
                        put("p_id", otroId)
                    }).decodeSingleOrNull<ContactoDto>()
                } catch (_: Exception) {
                    null
                }

                if (usuario != null) {
                    listaPreview.add(
                        ChatPreviewDto(
                            usuarioId = usuario.id_usuario,
                            nombreCompleto = "${usuario.nombre} ${usuario.apellido_paterno}",
                            ultimoMensaje = ultimoMsj?.contenido ?: "Sin mensajes aún",
                            fechaUltimoMensaje = conv.updated_at ?: conv.creado_en ?: ""
                        )
                    )
                }
            }
            listaPreview
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    suspend fun obtenerCalendarioEscolar(fechaFiltro: String): DiaEscolarDto? = withContext(Dispatchers.IO) {
        try {
            // Empaquetamos el parámetro de fecha estricta
            val parametrosQuery = buildJsonObject {
                put("fecha_buscada", fechaFiltro)
            }

            // Ejecutamos el RPC mapeando directamente al objeto único o nulo
            val resultado = SupabaseConnectionApp.client.postgrest
                .rpc("consultar_fecha_agenda_v2", parametrosQuery)
                .decodeSingleOrNull<DiaEscolarDto>() // Optimización: Evita deserializar listas innecesarias

            Log.d(
                "SUPABASE_AGENDA",
                "Consulta exitosa para $fechaFiltro. Registro encontrado: ${resultado != null}"
            )

            resultado
        } catch (e: Exception) {
            // Evitamos tragar la cancelación de la Coroutine
            if (e is kotlinx.coroutines.CancellationException) throw e

            Log.e("SUPABASE_AGENDA", "Error al consultar el calendario para $fechaFiltro: ${e.localizedMessage}", e)
            null
        }
    }

    suspend fun insertarEventoUsuario(evento: EventoInsertDto): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseConnectionApp.client.postgrest
                .from("sic_eventos")
                .insert(evento)

            Log.d("SUPABASE_INSERT", "Evento guardado exitosamente en el servidor: ${evento.titulo}")
            true
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e("SUPABASE_INSERT", "Error crítico al insertar el evento: ${e.localizedMessage}", e)
            false
        }
    }

    suspend fun selectEventosDeUsuario(fechaFiltro: String): List<EventoIdUsuarioDto> = withContext(Dispatchers.IO) {
        try {
            // 🔐 Obtenemos el ID del alumno logeado
            val usuarioId = SupabaseConnectionApp.client.auth.currentUserOrNull()?.id ?: return@withContext emptyList()

            // Hacemos el SELECT filtrando por el ID de usuario y la fecha de inicio
            val resultados = SupabaseConnectionApp.client.postgrest
                .from("sic_eventos")
                .select {
                    filter {
                        eq("id_usuario", usuarioId)
                        eq("fecha_inicio", fechaFiltro)
                    }
                    // 🚀 ORDEN DESCENDENTE por hora_inicio
                    order(column = "hora_inicio", order = Order.DESCENDING)
                }
                .decodeList<EventoIdUsuarioDto>()

            Log.d("SUPABASE_SELECT", "Eventos personales traídos para $fechaFiltro: ${resultados.size}")
            resultados
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e("SUPABASE_SELECT", "Error al traer eventos personales: ${e.localizedMessage}", e)
            emptyList()
        }
    }

    suspend fun obtenerHorarioAcademico(): List<HoraClaseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val usuario = obtenerMisDatos() ?: run {
                    Log.e("SUPABASE_REAL", "No se encontraron datos del usuario en sesión.")
                    return@withContext emptyList()
                }

                val uuidUsuarioLogueado = usuario.id_usuario ?: run {
                    Log.e("SUPABASE_REAL", "El usuario no cuenta con un id_usuario válido.")
                    return@withContext emptyList()
                }

                val esProfesor = usuario.id_tipo_usuario == 2
                val clientPostgrest = SupabaseConnectionApp.client.postgrest

                if (esProfesor) {
                    Log.d("SUPABASE_REAL", "Consultando vista de horarios para Profesor: $uuidUsuarioLogueado")
                    return@withContext clientPostgrest
                        .from("v_horario_completo")
                        .select {
                            filter { eq("id_profesor", uuidUsuarioLogueado) }
                        }.decodeList<HoraClaseDto>()
                } else {
                    Log.d("SUPABASE_REAL", "Consultando vista de horarios para Alumno: $uuidUsuarioLogueado")
                    return@withContext clientPostgrest
                        .from("v_horario_completo")
                        .select {
                            filter { eq("id_alumno", uuidUsuarioLogueado) }
                        }.decodeList<HoraClaseDto>()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_REAL", "Error crítico en v_horario_completo: ${e.localizedMessage}")
                emptyList()
            }
        }
    }






}
