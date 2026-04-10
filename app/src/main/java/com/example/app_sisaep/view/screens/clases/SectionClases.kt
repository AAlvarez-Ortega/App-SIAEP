package com.example.app_sisaep.view.screens.clases

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SectionClases() {
    val clasesDummy = listOf("Matemáticas", "Programación", "Física", "Base de Datos", "Ética")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Mis Clases", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(clasesDummy) { clase ->
                Card(
                    modifier = Modifier.size(width = 140.dp, height = 100.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(clase, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}