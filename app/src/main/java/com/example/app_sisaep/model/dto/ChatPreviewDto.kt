package com.example.app_sisaep.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatPreviewDto(
    val usuarioId: String,
    val nombreCompleto: String,
    val ultimoMensaje: String,
    val fechaUltimoMensaje: String, // Para ordenar
    val leido: Boolean = true
)