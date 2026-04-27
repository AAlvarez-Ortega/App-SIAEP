package com.example.app_sisaep.view.screens.noticias

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.app_sisaep.R

data class Deporte(val nombre: String, val iconRes: Int)

@Composable
fun DeportesSection() {
    Text(
        text = stringResource(R.string.sports),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    val deportes = listOf(
        Deporte(stringResource(R.string.sport_football_americano), R.drawable.ic_futbol_americano),
        Deporte(stringResource(R.string.sport_football), R.drawable.ic_futbol),
        Deporte(stringResource(R.string.sport_basketball), R.drawable.ic_basketnall),
        Deporte(stringResource(R.string.sport_tocho), R.drawable.ic_tocho)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(deportes) { deporte ->
            DeporteCard(deporte)
        }
    }
}

@Composable
fun DeporteCard(deporte: Deporte) {
    val circleColor = if (MaterialTheme.colorScheme.background == Color(0xFF121212)) {
        Color(0xFF616161) // gris en modo oscuro
    } else {
        Color(0xFF4CAF50) // verde en modo claro
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = circleColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = deporte.iconRes),
                    contentDescription = deporte.nombre,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = deporte.nombre,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}