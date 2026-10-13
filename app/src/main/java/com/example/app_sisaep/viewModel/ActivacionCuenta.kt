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

    fun errorCodigo(codigo: String): String? = if (Regex("^[0-9]{6}$").matches(codigo.trim())) null
        else "Escribe el código de seis dígitos que recibiste por correo."

    fun mensajeError(error: Throwable): String {
        val msg = error.message.orEmpty().lowercase()
        return when {
            "identidad_no_validada_en_padron" in msg -> "Los datos de la cuenta no coinciden con el registro aprobado. Solicita una revisión al responsable de tu escuela."
            "otp_expired" in msg || "token has expired" in msg || "one-time token not found" in msg -> "El código venció, ya se usó o no corresponde a esta cuenta. Usa sólo el más reciente. Si ya confirmaste tu correo, vuelve a iniciar sesión con tu contraseña."
            "already registered" in msg || "user_already_exists" in msg -> "Ya hay una cuenta con este correo. Vuelve al inicio e inicia sesión con tu contraseña."
            "email_address_not_authorized" in msg || "email address not authorized" in msg -> "El servidor de correo aún no permite enviar a esta dirección. El administrador debe configurar SMTP; tu aprobación se conserva."
            "rate" in msg || "over_email_send_rate_limit" in msg -> "Se alcanzó el límite temporal de correos. Espera antes de volver a intentarlo."
            else -> "No se pudo completar la operación. Comprueba la conexión; si ya confirmaste tu correo, puedes iniciar sesión."
        }
    }
}
