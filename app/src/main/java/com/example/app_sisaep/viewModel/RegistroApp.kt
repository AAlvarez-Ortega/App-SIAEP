package com.example.app_sisaep.viewModel

import com.example.app_sisaep.model.dto.ComprobanteSolicitud
import com.example.app_sisaep.model.dto.EscuelaDto
import com.example.app_sisaep.model.dto.SolicitudIdDto
import com.example.app_sisaep.model.dto.SolicitudInsertDto
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
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class SolicitudDuplicadaException : IllegalStateException("SOLICITUD_DUPLICADA")

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
