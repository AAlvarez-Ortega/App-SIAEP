package com.example.app_sisaep

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.app_sisaep.ui.theme.AppSisaepTheme
import com.example.app_sisaep.view.navigation.AppNavHost

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        setContent {
            val navController = rememberNavController()
            var darkMode by remember { mutableStateOf(false) }

            AppSisaepTheme(darkTheme = darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavHost(
                        navController = navController,
                        darkMode = darkMode,
                        onDarkModeChange = { darkMode = it }
                    )
                }
            }
        }
    }
}
