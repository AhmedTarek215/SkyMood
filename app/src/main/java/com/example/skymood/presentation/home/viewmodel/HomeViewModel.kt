package com.example.skymood.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.skymood.data.weather.WeatherRepository
import com.example.skymood.data.weather.model.WeatherResponse
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: WeatherRepository) : ViewModel() {

    val weatherData: StateFlow<WeatherResponse?> = repository.weatherState

    fun requestWeather(lat: Double, lon: Double, apiKey: String, unit: String, lang: String) {
        viewModelScope.launch {
            repository.fetchWeather(lat, lon, apiKey, unit, lang)
        }
    }
}

class HomeViewModelFactory(val repository: WeatherRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository) as T
    }
}
