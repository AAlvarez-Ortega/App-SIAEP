package com.example.app_sisaep.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class HoraClaseDto(
    val id_alumno: String? = null,
    val id_profesor: String,
    val id_escuela: String? = null,
    val id_plane: Int,
    val id_carrera: Int,
    val id_cicloesc: Int,
    val id_secuencia: String,
    val id_asignatura: String,
    val asignatura_abreviatura: String? = null,
    val asignatura_descripcion: String? = null,
    val turno: String? = null,
    val id_dia_num: String? = null,
    val nombre_dia: String? = null,
    val id_horas: Int,
    val ini_horas: String? = null,
    val fin_horas: String? = null,
    val id_edificio: Int,
    val edificio_nombre: String? = null,
    val edificio_siglas: String? = null,
    val id_salones: Int,
    val numero_salon: String? = null,
    val salon_nombre: String? = null,

    // NUEVOS CAMPOS MUTABLES PARA GUARDAR LA FUSIÓN
    var hora_inicio_fusionada: String? = null,
    var hora_fin_fusionada: String? = null
)