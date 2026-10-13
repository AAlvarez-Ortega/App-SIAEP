package com.example.app_sisaep.viewModel

import com.example.app_sisaep.model.dto.SolicitudPropiaDto
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object ActivacionCuenta {
    fun metadatos(solicitud: SolicitudPropiaDto) = buildJsonObject {
        require(solicitud.estado == "Aceptado") { "SOLICITUD_NO_APROBADA" }
        val d = solicitud.datos
        put("nombre", d.nombre)
        put("apellido_paterno", d.apellidoPaterno)
        put("apellido_materno", d.apellidoMaterno)
        put("boleta_o_empleado", d.boletaOEmpleado)
        put("curp", d.curp)
        put("escuela_cct", d.escuelaId)
        // Los roles se asignan en el servidor; no se envían desde el formulario.
    }

    fun errorPassword(password: String, confirmacion: String): String? = when {
        password.length < 8 -> "Usa una contraseña de al menos 8 caracteres."
        password != confirmacion -> "Las contraseñas no coinciden."
        else -> null
    }
}
