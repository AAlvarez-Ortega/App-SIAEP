package com.example.app_sisaep.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class MensajeDto(
    val id: String? = null,
    val conversacion_id: String,
    val remitente_id: String,
    val contenido: String,
    val leido: Boolean = false,
    val creado_en: String? = null
)