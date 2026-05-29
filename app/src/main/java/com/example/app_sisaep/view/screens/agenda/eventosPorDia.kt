package com.example.app_sisaep.view.screens.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app_sisaep.model.dto.DiaEscolarDto

@Composable
fun EventosPorDia(
    diaCoincidente: DiaEscolarDto?, // 📥 Recibe el día mapeado desde Supabase
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 18.dp)
    ) {
        if (diaCoincidente != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color dinámico para la barra lateral
                        val colorIndicador = when (diaCoincidente.id_tipodias) {
                            1, 10 -> Color(0xFF4CAF50) // Inscripción (Verde)
                            2, 3  -> Color(0xFF2196F3) // Inicio Periodo (Azul)
                            4     -> Color(0xFFF44336) // Fin Periodo (Rojo)
                            13    -> Color(0xFFFFEB3B) // Vacaciones (Amarillo)
                            14, 15 -> Color(0xFF9C27B0) // Asuetos (Morado)
                            else  -> Color(0xFF8A1F4D)  // Guinda institucional
                        }

                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(54.dp)
                                .background(colorIndicador, RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = diaCoincidente.descripcionActividad,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Calendario Oficial • Actividad Tipo ${diaCoincidente.id_tipodias}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay actividades programadas para este día",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}