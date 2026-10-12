package com.example.app_sisaep.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RolUsuarioTest {
    @Test fun conservaLosIdentificadoresPersistidos() {
        assertEquals(RolUsuario.ALUMNO, RolUsuario.desdeId(1))
        assertEquals(RolUsuario.MAESTRO, RolUsuario.desdeId(2))
        assertEquals(RolUsuario.DIRECTOR_ESCUELA, RolUsuario.desdeId(3))
        assertEquals(RolUsuario.ADMINISTRADOR_APLICACION, RolUsuario.desdeId(4))
        assertNull(RolUsuario.desdeId(0))
        assertNull(RolUsuario.desdeId(5))
    }
}
