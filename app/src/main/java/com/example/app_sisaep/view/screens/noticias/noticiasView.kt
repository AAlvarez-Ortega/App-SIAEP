package com.example.app_sisaep.view.screens.noticias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app_sisaep.view.screens.tarjetas.TarjetasEspecialesCarrusel

@Composable
fun InicioNoticias(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { TarjetasEspecialesCarrusel() }
        item { NoticiasSection() }
        item { DeportesSection() }
        item { ActividadesCulturalesSection() }
    }
}

