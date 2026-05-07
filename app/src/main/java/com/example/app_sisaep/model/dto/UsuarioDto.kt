package com.example.app_sisaep.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioDto(
    val id: String, // uuid
    val nombre: String,
    val apellido_paterno: String,
    val apellido_materno: String,
    val boleta_o_empleado: String,
    val correo: String,
    val curp: String,
    val id_tipo_usuario: Int,
    val escuela_cct: String,
    val creado_en: String? = null // timestamptz (opcional)
)