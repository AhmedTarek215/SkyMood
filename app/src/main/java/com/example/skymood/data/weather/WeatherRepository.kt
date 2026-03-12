package com.example.skymood.data.weather

import android.content.Context
import com.example.skymood.data.weather.datasource.local.WeatherLocalDataSource
import com.example.skymood.data.weather.datasource.remote.WeatherRemoteDataSource
import com.example.skymood.data.weather.model.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WeatherRepository(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val localDataSource: WeatherLocalDataSource
) {
    private val _weatherState = MutableStateFlow<WeatherResponse?>(null)
    val weatherState: StateFlow<WeatherResponse?> = _weatherState

    suspend fun fetchWeather(lat: Double, lon: Double, apiKey: String, unit: String, lang: String): Boolean {
        return try {
            val weatherData = remoteDataSource.getForecast(lat, lon, apiKey, unit, lang)
            if (weatherData != null) {
                localDataSource.saveWeather(weatherData)
            }
            _weatherState.emit(weatherData)
            false // Not offline
        } catch (e: Exception) {
            val cachedData = localDataSource.getCachedWeather()
            _weatherState.emit(cachedData)
            true // Is offline (or error)
        }
    }
}