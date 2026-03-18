package com.example.app_sisaep.view.screens

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavController
import com.example.app_sisaep.R

private val Guinda = Color(0xFF7A003C)

@Composable
fun ConfigScreen(
    navController: NavController,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    val activity = LocalContext.current as? Activity
    val currentLanguage = AppCompatDelegate.getApplicationLocales()[0]?.language ?: "es"
    var selectedLanguage by remember(currentLanguage) { mutableStateOf(currentLanguage) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val subtitleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val inactiveBoxColor = MaterialTheme.colorScheme.surfaceVariant
    val inactiveTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = onBackgroundColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Guinda
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.language_description),
            style = MaterialTheme.typography.bodyMedium,
            color = subtitleColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                LanguageOption(
                    title = stringResource(R.string.spanish),
                    subtitle = "Español",
                    selected = selectedLanguage == "es",
                    inactiveBoxColor = inactiveBoxColor,
                    inactiveTextColor = inactiveTextColor,
                    subtitleColor = subtitleColor,
                    textColor = onSurfaceColor,
                    onClick = {
                        if (selectedLanguage != "es") {
                            selectedLanguage = "es"
                            setAppLanguage("es")
                            activity?.recreate()
                        }
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = dividerColor
                )

                LanguageOption(
                    title = stringResource(R.string.english),
                    subtitle = "English",
                    selected = selectedLanguage == "en",
                    inactiveBoxColor = inactiveBoxColor,
                    inactiveTextColor = inactiveTextColor,
                    subtitleColor = subtitleColor,
                    textColor = onSurfaceColor,
                    onClick = {
                        if (selectedLanguage != "en") {
                            selectedLanguage = "en"
                            setAppLanguage("en")
                            activity?.recreate()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.appearance),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Guinda
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.appearance_description),
            style = MaterialTheme.typography.bodyMedium,
            color = subtitleColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            DarkModeOption(
                title = stringResource(R.string.dark_mode),
                subtitle = stringResource(R.string.dark_mode_description),
                checked = darkMode,
                inactiveBoxColor = inactiveBoxColor,
                subtitleColor = subtitleColor,
                textColor = onSurfaceColor,
                onCheckedChange = onDarkModeChange
            )
        }
    }
}

@Composable
private fun LanguageOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    inactiveBoxColor: Color,
    inactiveTextColor: Color,
    subtitleColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (selected) Guinda.copy(alpha = 0.12f) else inactiveBoxColor
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (title.lowercase().contains("span")) "ES" else "EN",
                color = if (selected) Guinda else inactiveTextColor,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor
            )
        }

        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = Guinda,
                unselectedColor = subtitleColor
            )
        )
    }
}

@Composable
private fun DarkModeOption(
    title: String,
    subtitle: String,
    checked: Boolean,
    inactiveBoxColor: Color,
    subtitleColor: Color,
    textColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (checked) Guinda.copy(alpha = 0.12f) else inactiveBoxColor
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🌙",
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Guinda
            )
        )
    }
}

fun setAppLanguage(languageCode: String) {
    val appLocale = LocaleListCompat.forLanguageTags(languageCode)
    AppCompatDelegate.setApplicationLocales(appLocale)
}