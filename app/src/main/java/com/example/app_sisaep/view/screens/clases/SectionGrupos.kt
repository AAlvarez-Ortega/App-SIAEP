package com.example.app_sisaep.view.screens.clases

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_sisaep.R

@Composable
fun SectionGrupos(
    navController: NavController
) {

    val gruposDummy = listOf(
        "6NM1",
        "6NM2",
        "5NM1",
        "4NM3",
        "1NM1"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {

        Text(
            text = stringResource(R.string.my_groups),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(gruposDummy) { grupo ->

                Card(
                    onClick = {
                        navController.navigate(
                            "chat_grupo/$grupo/$grupo"
                        )
                    },
                    modifier = Modifier.size(
                        width = 100.dp,
                        height = 80.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {

                        Text(
                            text = grupo,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}