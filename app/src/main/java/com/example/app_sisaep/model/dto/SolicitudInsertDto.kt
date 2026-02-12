package com.example.app_sisaep.model.dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SolicitudInsertDto(
    val nombre: String,

    @SerialName("apellido_paterno")
    val apellidoPaterno: String,

    @SerialName("apellido_materno")
    val apellidoMaterno: String,

    // ✅ CAMBIO: antes "numero_boleta"
    @SerialName("boleta_o_empleado")
    val boletaOEmpleado: String,

    val correo: String,
    val curp: String,

    @SerialName("escuela_id")
    val escuelaId: String
)
