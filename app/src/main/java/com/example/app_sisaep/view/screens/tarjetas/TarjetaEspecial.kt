package com.example.app_sisaep.view.screens.tarjetas

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

enum class CardType { HIMNO, GENERICA }

data class CardData(
    val titulo: String,
    val color: Color,
    @DrawableRes val imagenRes: Int,   // 👈 ya no es nullable
    val type: CardType = CardType.GENERICA
)



