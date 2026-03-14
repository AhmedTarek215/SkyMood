package com.example.skymood.data.weather

import android.content.Context
import com.example.skymood.data.weather.datasource.local.WeatherLocalDataSource
import com.example.skymood.data.weather.datasource.remote.WeatherRemoteDataSource
import com.example.skymood.data.weather.model.GeocodingResponse
import com.example.skymood.data.weather.model.WeatherResponse
import com.example.skymood.data.database.FavoriteEntity
import com.example.skymood.data.database.AlertEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers

class WeatherRepository(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val localDataSource: WeatherLocalDataSource
) {
    private val _weatherState = MutableStateFlow<WeatherResponse?>(null)
    val weatherState: StateFlow<WeatherResponse?> = _weatherState

    fun fetchWeatherFlow(
        lat: Double,
        lon: Double,
        apiKey: String,
        unit: String,
        lang: String,
        isHomeLocation: Boolean = true
    ): Flow<WeatherResponse?> = flow {
        val weatherData = remoteDataSource.getForecast(lat, lon, apiKey, unit, lang)
        if (weatherData != null && isHomeLocation) {
            localDataSource.saveWeather(weatherData)
            _weatherState.emit(weatherData)
        }
        emit(weatherData)
    }.catch { e ->
        if (isHomeLocation) {
            val cachedData = localDataSource.getCachedWeather()
            emit(cachedData)
        } else {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun fetchWeather(lat: Double, lon: Double, apiKey: String, unit: String, lang: String, isHomeLocation: Boolean = true): Boolean {
        return try {
            val weatherData = remoteDataSource.getForecast(lat, lon, apiKey, unit, lang)
            if (weatherData != null && isHomeLocation) {
                localDataSource.saveWeather(weatherData)
            }
            _weatherState.emit(weatherData)
            return false
        } catch (e: Exception) {
            if (isHomeLocation) {
                val cachedData = localDataSource.getCachedWeather()
                _weatherState.emit(cachedData)
            }
            return true
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

    suspend fun insertAlert(alertEntity: AlertEntity): Long {
        return localDataSource.insertAlert(alertEntity)
    }

    suspend fun deleteAlert(alertEntity: AlertEntity) {
        localDataSource.deleteAlert(alertEntity)
    }

    suspend fun deleteAlertById(id: Int) {
        localDataSource.deleteAlertById(id)
    }

    suspend fun updateAlertEnabled(id: Int, isEnabled: Boolean) {
        localDataSource.updateAlertEnabled(id, isEnabled)
    }

    fun getAlerts(): Flow<List<AlertEntity>> {
        return localDataSource.getAlerts()
    }

    suspend fun getAlertById(id: Int): AlertEntity? {
        return localDataSource.getAlertById(id)
    }
}