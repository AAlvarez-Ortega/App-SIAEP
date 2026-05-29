package com.example.app_sisaep.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class DiaEscolarDto(
    val id_escfecha: String,             // Recibe el texto directo de la función
    val id_tipodias: Int,                // Recibe el número directo de la función
    val descripcion_actividad: String    // Recibe el texto de la descripción ya unido desde SQL
) {
    // Propiedad calculada de respaldo para mantener compatibilidad total con tu UI actual
    val descripcionActividad: String
        get() = descripcion_actividad
}