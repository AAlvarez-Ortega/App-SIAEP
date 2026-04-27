package com.example.app_sisaep.view.screens.noticias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app_sisaep.view.screens.tarjetas.TarjetasEspecialesCarrusel

@Composable
fun InicioNoticias(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 110.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { TarjetasEspecialesCarrusel() }
        item { NoticiasSection() }
        item { DeportesSection() }
        item { ActividadesCulturalesSection() }
    }
}

