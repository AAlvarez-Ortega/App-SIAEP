package com.example.app_sisaep.view.screens.clases

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class ClaseDummy(
    val id: String,
    val nombre: String
)

@Composable
fun SectionClases(
    navController: NavController,
    clases: List<ClaseDummy>
) {

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),

        contentPadding = PaddingValues(horizontal = 6.dp),

        modifier = Modifier.fillMaxWidth()
    ) {

        items(clases) { clase ->

            Card(
                modifier = Modifier
                    .width(190.dp)
                    .height(105.dp)
                    .clickable {

                        navController.navigate(
                            "tablon_clase/${clase.id}/${clase.nombre}"
                        )

                    },

                shape = RoundedCornerShape(18.dp),

                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),

                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = 14.dp,
                            vertical = 10.dp
                        ),

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = clase.nombre,

                        color = MaterialTheme.colorScheme.onSecondaryContainer,

                        fontSize = 15.sp,

                        fontWeight = FontWeight.SemiBold,

                        textAlign = TextAlign.Center,

                        maxLines = 3,

                        overflow = TextOverflow.Ellipsis,

                        lineHeight = 19.sp
                    )

                }

            }

        }

    }

}