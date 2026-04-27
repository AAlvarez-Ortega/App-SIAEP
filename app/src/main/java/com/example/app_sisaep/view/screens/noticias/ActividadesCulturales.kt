package com.example.app_sisaep.view.screens.noticias


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

data class Actividad(val nombre: String, val iconRes: Int)

@Composable
fun ActividadesCulturalesSection() {
    Text(
        text = stringResource(R.string.cultural_activities),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    val actividades = listOf(
        Actividad(stringResource(R.string.activity_theater), R.drawable.ic_teatro),
        Actividad(stringResource(R.string.activity_music), R.drawable.ic_musica),
        Actividad(stringResource(R.string.activity_dance), R.drawable.ic_danza),
        Actividad(stringResource(R.string.activity_photography), R.drawable.ic_fotografia)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(actividades) { actividad ->
            ActividadCard(actividad)
        }
    }
}

@Composable
fun ActividadCard(actividad: Actividad) {

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
                    painter = painterResource(id = actividad.iconRes),
                    contentDescription = actividad.nombre,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = actividad.nombre,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}