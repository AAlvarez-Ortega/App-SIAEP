package com.example.app_sisaep.model.dto

import kotlinx.serialization.Serializable

/** Datos del directorio escolar; la identidad completa sólo se consulta en el perfil propio. */
@Serializable
data class ContactoDto(
    val id_usuario: String,
    val nombre: String,
    val apellido_paterno: String,
    val apellido_materno: String
)
