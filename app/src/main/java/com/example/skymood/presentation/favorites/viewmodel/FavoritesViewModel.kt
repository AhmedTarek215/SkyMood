package com.example.skymood.presentation.favorites.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.skymood.data.database.FavoriteEntity
import com.example.skymood.data.settings.SettingsPreferencesManager
import com.example.skymood.data.weather.WeatherRepository
import com.example.skymood.utils.Constants
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: WeatherRepository,
    private val preferencesManager: SettingsPreferencesManager
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteEntity>> = repository.getFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun fetchAndAddFavorite(lat: Double, lon: Double) {
        viewModelScope.launch {
            val tempPref = preferencesManager.temperatureUnitStream.first()
            val langPref = preferencesManager.appLanguageStream.first()
            val unitParam = when (tempPref) {
                "celsius" -> "metric"
                "fahrenheit" -> "imperial"
                else -> "standard"
            }
            val response = repository.fetchForecastForFavorite(lat, lon, Constants.WEATHER_API_KEY, unitParam, langPref)
            if (response != null) {
                val favorite = FavoriteEntity(
                    cityName = response.city.name,
                    countryName = response.city.country,
                    lat = lat,
                    lon = lon
                )
                repository.insertFavorite(favorite)
            }
        }
    }

    fun addFavorite(favorite: FavoriteEntity) {
        viewModelScope.launch {
            repository.insertFavorite(favorite)
        }
    }

    fun removeFavorite(favorite: FavoriteEntity) {
        viewModelScope.launch {
            repository.deleteFavorite(favorite)
        }
    }
}

class FavoritesViewModelFactory(
    private val repository: WeatherRepository,
    private val preferencesManager: SettingsPreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FavoritesViewModel(repository, preferencesManager) as T
    }
}
