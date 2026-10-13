package com.example.app_sisaep.viewModel

import com.example.app_sisaep.model.dto.ComprobanteSolicitud
import com.example.app_sisaep.model.dto.EscuelaDto
import com.example.app_sisaep.model.dto.SolicitudIdDto
import com.example.app_sisaep.model.dto.SolicitudInsertDto
import com.example.app_sisaep.model.dto.SolicitudPropiaDto
import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class SolicitudDuplicadaException : IllegalStateException("SOLICITUD_DUPLICADA")
class SolicitudCambioException : IllegalStateException("SOLICITUD_CAMBIO")

object RegistroApp {
    fun nuevoComprobante() = ComprobanteSolicitud(
        id = UUID.randomUUID().toString(),
        token = (UUID.randomUUID().toString() + UUID.randomUUID()).replace("-", "")
    )

    private suspend fun <T> conTiempoLimite(block: suspend () -> T): T = try {
        withTimeout(15_000L) { block() }
    } catch (e: TimeoutCancellationException) {
        currentCoroutineContext().ensureActive()
        throw IOException("No se pudo completar la consulta de registro", e)
    }

    suspend fun escuelas(): List<EscuelaDto> = conTiempoLimite {
        SupabaseConnectionApp.client.postgrest.rpc("sisaep_catalogo_escuelas")
            .decodeList<EscuelaDto>()
    }

    suspend fun enviar(payload: SolicitudInsertDto, comprobante: ComprobanteSolicitud): String =
        conTiempoLimite {
            val parametros = buildJsonObject {
                put("p_id", comprobante.id)
                put("p_token", comprobante.token)
                putJsonObject("p_solicitud") {
                    put("nombre", payload.nombre)
                    put("apellido_paterno", payload.apellidoPaterno)
                    put("apellido_materno", payload.apellidoMaterno)
                    put("boleta_o_empleado", payload.boletaOEmpleado)
                    put("correo", payload.correo)
                    put("curp", payload.curp)
                    put("escuela_id", payload.escuelaId)
                }
            }
            try {
                SupabaseConnectionApp.client.postgrest.rpc("sisaep_crear_solicitud", parametros)
                    .decodeSingle<SolicitudIdDto>().id
            } catch (e: Exception) {
                if (e.message?.contains("SOLICITUD_DUPLICADA") == true) {
                    throw SolicitudDuplicadaException()
                }
                throw e
            }
        }

    suspend fun propia(comprobante: ComprobanteSolicitud): SolicitudPropiaDto? = conTiempoLimite {
        val parametros = buildJsonObject {
            put("p_id", comprobante.id)
            put("p_token", comprobante.token)
        }
        val respuesta = SupabaseConnectionApp.client.postgrest.rpc("sisaep_solicitud_propia", parametros)
            .decodeAs<JsonElement>()
        if (respuesta == JsonNull) null else Json.decodeFromJsonElement<SolicitudPropiaDto>(respuesta)
    }

    suspend fun corregir(payload: SolicitudInsertDto, comprobante: ComprobanteSolicitud, version: Int): SolicitudPropiaDto =
        conTiempoLimite {
            val parametros = buildJsonObject {
                put("p_id", comprobante.id)
                put("p_token", comprobante.token)
                put("p_version", version)
                putJsonObject("p_solicitud") {
                    put("nombre", payload.nombre)
                    put("apellido_paterno", payload.apellidoPaterno)
                    put("apellido_materno", payload.apellidoMaterno)
                    put("boleta_o_empleado", payload.boletaOEmpleado)
                    put("correo", payload.correo)
                    put("curp", payload.curp)
                    put("escuela_id", payload.escuelaId)
                }
            }
            try {
                SupabaseConnectionApp.client.postgrest.rpc("sisaep_corregir_solicitud", parametros)
                    .decodeAs<SolicitudPropiaDto>()
            } catch (e: Exception) {
                if (e.message?.contains("SOLICITUD_DUPLICADA") == true) throw SolicitudDuplicadaException()
                if (e.message?.contains("SOLICITUD_CAMBIO") == true || e.message?.contains("SOLICITUD_NO_EDITABLE") == true) {
                    throw SolicitudCambioException()
                }
                throw e
            }
        }

    suspend fun estado(comprobante: ComprobanteSolicitud): String? = conTiempoLimite {
        val parametros = buildJsonObject {
            put("p_id", comprobante.id)
            put("p_token", comprobante.token)
        }
        SupabaseConnectionApp.client.postgrest.rpc("sisaep_estado_solicitud", parametros)
            .decodeList<Map<String, String>>()
            .firstOrNull()?.get("estado")
    }
}
