package com.example.skymood.data.weather

import android.content.Context
import com.example.skymood.data.weather.datasource.remote.WeatherRemoteDataSource
import com.example.skymood.data.weather.model.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WeatherRepository(context: Context) {
    private val weatherRemoteDataSource = WeatherRemoteDataSource()
    private val _weatherState = MutableStateFlow<WeatherResponse?>(null)
    val weatherState: StateFlow<WeatherResponse?> = _weatherState

    suspend fun fetchWeather(lat: Double, lon: Double, apiKey: String) {
        try {
            val weatherData = weatherRemoteDataSource.getForecast(lat, lon, apiKey, "metric", "en")
            _weatherState.emit(weatherData)
        } catch (e: Exception) {
            _weatherState.emit(null)
        }
    }
}