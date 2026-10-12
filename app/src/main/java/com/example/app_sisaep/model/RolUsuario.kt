package com.example.app_sisaep.model

/** Identificadores persistidos en sic_tusuario. Los permisos se validan en Supabase. */
enum class RolUsuario(val id: Int, val codigoQr: String) {
    ALUMNO(1, "ALUMNO"),
    MAESTRO(2, "MAESTRO"),
    DIRECTOR_ESCUELA(3, "DIRECTOR_ESCUELA"),
    ADMINISTRADOR_APLICACION(4, "ADMINISTRADOR_APLICACION");

    companion object {
        fun desdeId(id: Int): RolUsuario? = entries.firstOrNull { it.id == id }
    }
}
