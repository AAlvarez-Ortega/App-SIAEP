package com.example.app_sisaep

import com.example.app_sisaep.model.dto.SolicitudInsertDto
import com.example.app_sisaep.model.dto.SolicitudPropiaDto
import com.example.app_sisaep.viewModel.ActivacionCuenta
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.*
import org.junit.Test

class ActivacionCuentaTest {
    private val solicitud = SolicitudPropiaDto("fixture", 2, "Aceptado",
        SolicitudInsertDto("PRUEBA", "ALTA", "LOCAL", "9900000010", "qa@example.com", "TEST000101HDFXXX01", "09DPN6203B"))

    @Test fun metadataUsesSchoolCctWithoutAdministrativeRole() {
        val metadata = ActivacionCuenta.metadatos(solicitud)
        assertEquals("09DPN6203B", metadata["escuela_cct"]?.jsonPrimitive?.content)
        assertFalse(metadata.containsKey("escuela_id"))
        assertFalse(metadata.containsKey("rol"))
        assertFalse(metadata.containsKey("id_tipo_usuario"))
        assertFalse(metadata.containsKey("password"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun pendingRequestCannotActivate() { ActivacionCuenta.metadatos(solicitud.copy(estado = "Pendiente")) }

    @Test fun passwordConfirmationAndLength() {
        assertNotNull(ActivacionCuenta.errorPassword("short", "short"))
        assertNotNull(ActivacionCuenta.errorPassword("12345678910", "12345678911"))
        assertNull(ActivacionCuenta.errorPassword("12345678910", "12345678910"))
    }

    @Test fun otpRequiresSixAsciiDigits() {
        assertNull(ActivacionCuenta.errorCodigo("123456"))
        assertNull(ActivacionCuenta.errorCodigo(" 123456 "))
        for (bad in listOf("", "12345", "1234567", "abc123", "１２３４５６")) {
            assertNotNull(ActivacionCuenta.errorCodigo(bad))
        }
    }

    @Test fun usedCodeGuidesExistingAccountsToLogin() {
        val text = ActivacionCuenta.mensajeError(IllegalStateException("otp_expired: Token has expired"))
        assertTrue(text.contains("ya se usó"))
        assertTrue(text.contains("Si ya confirmaste"))
    }

    @Test fun smtpAndIdentityErrorsAreDifferent() {
        assertTrue(ActivacionCuenta.mensajeError(IllegalStateException("email_address_not_authorized")).contains("SMTP"))
        assertTrue(ActivacionCuenta.mensajeError(IllegalStateException("IDENTIDAD_NO_VALIDADA_EN_PADRON")).contains("registro aprobado"))
    }
}
