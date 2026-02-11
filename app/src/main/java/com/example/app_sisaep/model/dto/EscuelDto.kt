package com.example.app_sisaep.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EscuelaDto(
    val id: String,
    val nombre: String,
    val siglas: String? = null,
    @SerialName("creado_en") val creadoEn: String? = null
)
