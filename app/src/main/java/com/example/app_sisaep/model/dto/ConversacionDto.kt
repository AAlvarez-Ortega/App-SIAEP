package com.example.app_sisaep.model.dto
import kotlinx.serialization.Serializable


@Serializable
data class  ConversacionDto(
    val id: String,
    val usuario_1: String,
    val usuario_2: String,
    // Usamos nombres opcionales y valores por defecto para que no truene
    val creado_en: String? = null,
    val updated_at: String? = null
)