package com.example.app_sisaep.view.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavController
import com.example.app_sisaep.R

import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable

@Composable
fun ConfigScreen(navController: NavController) {
    val activity = LocalContext.current as? Activity
    val currentLanguage = AppCompatDelegate.getApplicationLocales()[0]?.language ?: "es"
    var selectedLanguage by remember(currentLanguage) { mutableStateOf(currentLanguage) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.settings))
        Text(stringResource(R.string.language))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    selectedLanguage = "es"
                    setAppLanguage("es")
                    activity?.recreate()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedLanguage == "es",
                onClick = null
            )
            Text(stringResource(R.string.spanish))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    selectedLanguage = "en"
                    setAppLanguage("en")
                    activity?.recreate()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedLanguage == "en",
                onClick = null
            )
            Text(stringResource(R.string.english))
        }
    }
}

fun setAppLanguage(languageCode: String) {
    val appLocale = LocaleListCompat.forLanguageTags(languageCode)
    AppCompatDelegate.setApplicationLocales(appLocale)
}