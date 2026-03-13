package com.example.skymood.data.weather

import android.content.Context
import com.example.skymood.data.weather.datasource.local.WeatherLocalDataSource
import com.example.skymood.data.weather.datasource.remote.WeatherRemoteDataSource
import com.example.skymood.data.weather.model.GeocodingResponse
import com.example.skymood.data.weather.model.WeatherResponse
import com.example.skymood.data.database.FavoriteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow

class WeatherRepository(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val localDataSource: WeatherLocalDataSource
) {
    private val _weatherState = MutableStateFlow<WeatherResponse?>(null)
    val weatherState: StateFlow<WeatherResponse?> = _weatherState

    suspend fun fetchWeather(lat: Double, lon: Double, apiKey: String, unit: String, lang: String, isHomeLocation: Boolean = true): Boolean {
        return try {
            val weatherData = remoteDataSource.getForecast(lat, lon, apiKey, unit, lang)
            if (weatherData != null && isHomeLocation) {
                localDataSource.saveWeather(weatherData)
            }
            _weatherState.emit(weatherData)
            false // Not offline
        } catch (e: Exception) {
            if (isHomeLocation) {
                val cachedData = localDataSource.getCachedWeather()
                _weatherState.emit(cachedData)
            }
            true // Is offline (or error)
        }
    }

    suspend fun fetchForecastForFavorite(lat: Double, lon: Double, apiKey: String, unit: String, lang: String): WeatherResponse? {
        return try {
            remoteDataSource.getForecast(lat, lon, apiKey, unit, lang)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun searchCity(query: String, apiKey: String): List<GeocodingResponse> {
        return try {
            remoteDataSource.searchCity(query, apiKey) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun insertFavorite(favoriteEntity: FavoriteEntity) {
        localDataSource.insertFavorite(favoriteEntity)
    }

    suspend fun deleteFavorite(favoriteEntity: FavoriteEntity) {
        localDataSource.deleteFavorite(favoriteEntity)
    }

    fun getFavorites(): Flow<List<FavoriteEntity>> {
        return localDataSource.getFavorites()
    }
}