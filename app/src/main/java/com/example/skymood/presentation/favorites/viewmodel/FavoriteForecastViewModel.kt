package com.example.skymood.presentation.favorites.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.skymood.data.settings.SettingsPreferencesManager
import com.example.skymood.data.weather.WeatherRepository
import com.example.skymood.data.weather.model.WeatherResponse
import com.example.skymood.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FavoriteForecastViewModel(
    private val repository: WeatherRepository,
    private val settingsPreferencesManager: SettingsPreferencesManager
) : ViewModel() {

    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _windUnit = MutableStateFlow("m/s")
    val windUnit: StateFlow<String> = _windUnit

    private val _temperatureUnit = MutableStateFlow("celsius")
    val temperatureUnit: StateFlow<String> = _temperatureUnit

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var currentLat: Double = 0.0
    private var currentLon: Double = 0.0

    fun fetchForecast(lat: Double, lon: Double) {
        currentLat = lat
        currentLon = lon
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val tempPref = settingsPreferencesManager.temperatureUnitStream.first()
                val lang = settingsPreferencesManager.appLanguageStream.first()
                
                _temperatureUnit.value = tempPref
                
                val unitParam = when (tempPref) {
                    "celsius" -> "metric"
                    "fahrenheit" -> "imperial"
                    else -> "standard"
                }
                val windPref = settingsPreferencesManager.windSpeedUnitStream.first()
                _windUnit.value = if (windPref == "mph") "mph" else "m/s"

                val response = repository.fetchForecastForFavorite(lat, lon, Constants.WEATHER_API_KEY, unitParam, lang)
                if (response != null) {
                    _weatherData.value = response
                } else {
                    _errorMessage.value = "Failed to fetch forecast"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        if (currentLat != 0.0 && currentLon != 0.0) {
            fetchForecast(currentLat, currentLon)
        }
    }
}

class FavoriteForecastViewModelFactory(
    private val repository: WeatherRepository,
    private val preferencesManager: SettingsPreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteForecastViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoriteForecastViewModel(repository, preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
