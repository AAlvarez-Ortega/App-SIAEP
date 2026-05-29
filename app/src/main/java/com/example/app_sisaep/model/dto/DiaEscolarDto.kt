package com.example.app_sisaep.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiaEscolarDto(
    val id_escfecha: String,
    val id_tipodias: Int
) {
    // 🧠 Propiedad dinámica que inyectaremos manualmente en la función de abajo
    var descripcionActividad: String = "Actividad Escolar"
}

@Serializable
data class CatalogoTipoDiaDto(
    val id_tipodias: Int,

    val descripcion: String
)