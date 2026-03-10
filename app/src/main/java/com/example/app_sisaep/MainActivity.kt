package com.example.app_sisaep

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.app_sisaep.view.navigation.AppNavHost

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        setContent {
            val navController = rememberNavController()

            Surface(
                modifier = Modifier,
                color = MaterialTheme.colorScheme.background
            ) {
                AppNavHost(navController = navController)
            }
        }
    }

}
