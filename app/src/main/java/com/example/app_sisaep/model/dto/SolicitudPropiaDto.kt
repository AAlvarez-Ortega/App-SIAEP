package com.example.app_sisaep.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class SolicitudPropiaDto(
    val id: String,
    val version: Int,
    val estado: String,
    val datos: SolicitudInsertDto
)
