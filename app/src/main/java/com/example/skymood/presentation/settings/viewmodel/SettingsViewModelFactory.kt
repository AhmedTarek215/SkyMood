package com.example.skymood.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.skymood.data.settings.SettingsPreferencesManager

class SettingsViewModelFactory(
    private val preferencesManager: SettingsPreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
