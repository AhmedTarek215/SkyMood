package com.example.skymood.presentation.home.viewmodel

import com.example.skymood.data.weather.model.WeatherResponse

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val data: WeatherResponse, val isOffline: Boolean = false) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

sealed class WeatherUiEvent {
    data class ShowError(val message: String) : WeatherUiEvent()
    data class ShowToast(val message: String) : WeatherUiEvent()
}
