package com.example.app_sisaep.viewModel

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.delay
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter



object QrGenerate {

    fun buildPayload(userId: String, idTipoUsuario: Int): String {
        val rolTexto = when (idTipoUsuario) {
            1 -> "ALUMNO"
            2 -> "PROFESOR"
            else -> "USUARIO"
        }

        val now = OffsetDateTime.now(ZoneId.of("America/Mexico_City"))
        val timestamp = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        return """{"id":"$userId","rol":"$rolTexto","hora":"$timestamp"}"""
    }

    // ESTA FUNCIÓN DEBE ESTAR AQUÍ DENTRO
    fun generateQrBitmap(contenido: String, width: Int = 900, height: Int = 900): Bitmap {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            contenido,
            BarcodeFormat.QR_CODE,
            width,
            height
        )
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    suspend fun dynamicQrLoop(
        userId: String,
        idTipoUsuario: Int,
        intervalMillis: Long = 10_000L,
        onNewBitmap: (Bitmap) -> Unit
    ) {
        if (userId.isBlank()) return
        while (true) {
            val payload = buildPayload(userId, idTipoUsuario)
            onNewBitmap(generateQrBitmap(payload)) // Aquí ya no marcará error
            delay(intervalMillis)
        }
    }
}