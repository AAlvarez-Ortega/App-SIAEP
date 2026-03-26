package com.example.app_sisaep.view.screens.Btncreateavisos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.app_sisaep.model.dto.AvisoGlobal
import androidx.compose.ui.res.stringResource
import com.example.app_sisaep.R

@Composable
fun PantallaCompletaAviso(aviso: AvisoGlobal, onConfirm: () -> Unit) {
    Surface(
        modifier = Modifier.Companion.fillMaxSize(),
        color = androidx.compose.ui.graphics.Color(0xFFB71C1C) // Rojo oscuro (tipo IPN/Emergencia)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.run { size(120.dp) },
                tint = androidx.compose.ui.graphics.Color.White
            )
            Text(
                text = stringResource(R.string.urgent_notice_title),
                style = MaterialTheme.typography.displaySmall,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = aviso.titulo,
                style = MaterialTheme.typography.headlineMedium,
                color = androidx.compose.ui.graphics.Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = aviso.mensaje,
                style = MaterialTheme.typography.bodyLarge,
                color = androidx.compose.ui.graphics.Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White, contentColor = androidx.compose.ui.graphics.Color.Black),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(
                    stringResource(R.string.understood_uppercase),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}