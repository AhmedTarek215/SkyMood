package com.example.skymood

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.example.skymood.data.settings.SettingsPreferencesManager
import com.example.skymood.presentation.MainScreen
import com.example.skymood.ui.theme.SkyMoodTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val preferencesManager = SettingsPreferencesManager(this)
        lifecycleScope.launch {
            val currentLang = preferencesManager.appLanguageStream.first()
            val localeList = LocaleListCompat.forLanguageTags(currentLang)
            AppCompatDelegate.setApplicationLocales(localeList)
        }

        enableEdgeToEdge()
        setContent {
            SkyMoodTheme {
                MainScreen()
            }
        }
    }
}
