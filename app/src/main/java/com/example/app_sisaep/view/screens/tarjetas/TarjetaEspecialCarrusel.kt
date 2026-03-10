package com.example.app_sisaep.view.screens.tarjetas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.app_sisaep.R
import com.example.app_sisaep.view.screens.noticias.HimnoIPNCard

import androidx.compose.ui.res.stringResource

@Composable
fun TarjetasEspecialesCarrusel() {
    val tarjetas = listOf(
        CardData(stringResource(R.string.ipn_anthem_title), Color(0xFF7F1E57), R.drawable.logo_ipn_blanco, CardType.HIMNO),
        CardData(stringResource(R.string.doctorate), Color(0xFF388E3C), R.drawable.ic_doctorado),
        CardData(stringResource(R.string.masters_administration), Color(0xFF1976D2), R.drawable.ic_maestria_admin),
        CardData(stringResource(R.string.masters_computer_science), Color(0xFFFBC02D), R.drawable.ic_maestria_info),
        CardData(stringResource(R.string.learning_units), Color(0xFF8E24AA), R.drawable.ic_unidades)
    )

    val listState = rememberLazyListState()
    val currentIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    // Estado compartido del himno 🎶
    val isPlaying = remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(tarjetas, key = { it.titulo }) { card ->
                when (card.type) {
                    CardType.HIMNO -> HimnoCardWrapper(isPlaying)
                    CardType.GENERICA -> GenericaCardConImagen(card)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // === Indicadores (puntitos) ===
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(tarjetas.size) { index ->
                val dotColor = if (index == currentIndex) Color(0xFF7F1E57) else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (index == currentIndex) 10.dp else 8.dp)
                        .background(dotColor, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun HimnoCardWrapper(isPlaying: MutableState<Boolean>) {
    Box(
        modifier = Modifier
            .width(300.dp)
            .height(200.dp)
    ) {
        HimnoIPNCard(isPlaying = isPlaying)
    }
}

