package com.example.skymood.presentation.settings.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skymood.data.settings.SettingsPreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val preferencesManager: SettingsPreferencesManager) : ViewModel() {

    val locationMethod = preferencesManager.locationMethodStream.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "gps"
    )
    val temperatureUnit = preferencesManager.temperatureUnitStream.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "celsius"
    )
    val windSpeedUnit = preferencesManager.windSpeedUnitStream.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "mps"
    )
    val appLanguage = preferencesManager.appLanguageStream.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "en"
    )

    fun setLocationMethod(method: String) {
        viewModelScope.launch { preferencesManager.saveLocationMethod(method) }
    }

    fun setTemperatureUnit(unit: String) {
        viewModelScope.launch { preferencesManager.saveTemperatureUnit(unit) }
    }

    fun setWindSpeedUnit(unit: String) {
        viewModelScope.launch { preferencesManager.saveWindSpeedUnit(unit) }
    }

    fun setAppLanguage(lang: String) {
        viewModelScope.launch { 
            preferencesManager.saveAppLanguage(lang) 
            val localeList = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }
}
