package com.example.app_sisaep.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class AvisoGlobal(

    val id: String,

    val titulo: String,

    val mensaje: String,

    val tipo_aviso: String? = null,

    val estado: String? = null,

    val fecha_expiracion: String? = null

)