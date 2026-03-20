package com.example.app_sisaep.view.screens.Btncreateavisos


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.app_sisaep.model.supabase.SupabaseConnectionApp
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun BtnCreateAviso(onClick: () -> Unit) {
    var isAdmin by remember { mutableStateOf(false) }
    var showModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userId = SupabaseConnectionApp.client.auth.currentUserOrNull()?.id
        if (userId != null) {
            val result = withContext(Dispatchers.IO) {
                try {
                    val response = SupabaseConnectionApp.client.from("personal_administrativo")
                        .select { filter { eq("id", userId) } }
                    response.data != "[]"
                } catch (e: Exception) { false }
            }
            isAdmin = result
        }
    }

    if (isAdmin) {
        FloatingActionButton(
            onClick = { showModal = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Crear Aviso")
        }
    }

    if (showModal) {
        LogCreateAviso(
            onDismiss = { showModal = false },
            onSuccess = {
                // Aquí podrías disparar una recarga de la lista de noticias si fuera necesario
            }
        )
    }
}