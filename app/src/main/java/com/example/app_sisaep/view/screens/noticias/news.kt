package com.example.app_sisaep.view.screens.noticias


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.app_sisaep.R

@Composable
fun NoticiasSection() {
    Text(
        text = stringResource(R.string.news),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    val noticias = listOf(
        stringResource(R.string.news_call),
        stringResource(R.string.news_payment),
        stringResource(R.string.news_workshops),
        stringResource(R.string.news_event),
        stringResource(R.string.news_notice)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(noticias) { noticia ->
            NoticiaCard(noticia)
        }
    }
}

@Composable
fun NoticiaCard(titulo: String) {
    Card(
        modifier = Modifier
            .size(width = 200.dp, height = 120.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(titulo, style = MaterialTheme.typography.bodyMedium)
        }
    }
}