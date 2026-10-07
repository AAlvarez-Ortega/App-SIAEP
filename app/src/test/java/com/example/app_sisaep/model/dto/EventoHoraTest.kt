package com.example.app_sisaep.model.dto

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId

class EventoHoraTest {
    private val zonaMexico = ZoneId.of("America/Mexico_City")

    @Test
    fun respuestaUtcDeSupabaseConservaLaHoraElegidaEnMexico() {
        assertEquals("03:00 PM", evento("2026-10-05T21:00:00+00:00")
            .obtenerHoraInicioFormateada(zonaMexico))
    }

    @Test
    fun respuestaConOffsetLocalRepresentaLaMismaHora() {
        assertEquals("03:00 PM", evento("2026-10-05T15:00:00-06:00")
            .obtenerHoraInicioFormateada(zonaMexico))
    }

    @Test
    fun conversionUsaElHusoDeLaFechaDelEvento() {
        // En julio de 2022 Ciudad de México todavía tenía horario de verano.
        assertEquals("03:00 PM", evento("2022-07-05T20:00:00Z")
            .obtenerHoraInicioFormateada(zonaMexico))
    }

    private fun evento(hora: String) = EventoIdUsuarioDto(
        idEvento = 1, idUsuario = "qa", fechaInicio = "2026-10-05",
        fechaFin = "2026-10-05", horaInicio = hora, horaFin = hora,
        titulo = "QA", lugar = null, notas = null, status = 1
    )
}
