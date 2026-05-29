package com.example.app_sisaep.model.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Serializable
data class EventoInsertDto(
    @SerialName("id_usuario") val idUsuario: String?, // UUID como String
    @SerialName("fecha_inicio") val fechaInicio: String, // "YYYY-MM-DD"
    @SerialName("fecha_fin") val fechaFin: String,       // "YYYY-MM-DD"
    @SerialName("hora_inicio") val horaInicio: String,   // ISO 8601 con zona (TIMESTAMPTZ)
    @SerialName("hora_fin") val horaFin: String,         // ISO 8601 con zona (TIMESTAMPTZ)
    @SerialName("titulo") val titulo: String,
    @SerialName("lugar") val lugar: String?,
    @SerialName("notas") val notas: String?
    // 'status' no se incluye porque la base de datos lo asigna automáticamente en 1 gracias al DEFAULT 1
)



@Serializable
data class EventoIdUsuarioDto(
    @SerialName("id_evento") val idEvento: Int,
    @SerialName("id_usuario") val idUsuario: String?,
    @SerialName("fecha_inicio") val fechaInicio: String,
    @SerialName("fecha_fin") val fechaFin: String,
    @SerialName("hora_inicio") val horaInicio: String, // Viene como "2026-05-29T09:00:00-06:00"
    @SerialName("hora_fin") val horaFin: String,
    @SerialName("titulo") val titulo: String,
    @SerialName("lugar") val lugar: String?,
    @SerialName("notas") val notas: String?,
    @SerialName("status") val status: Int
) {
    //Extensión para mostrar solo la hora bonita en la tarjeta (Ej: "09:00 AM")
    fun obtenerHoraInicioFormateada(): String {
        return try {
            val odt = OffsetDateTime.parse(horaInicio)
            odt.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
        } catch (_: Exception) {
            ""
        }
    }
}