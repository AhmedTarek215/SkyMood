package com.example.skymood.data.weather.datasource.local

import com.example.skymood.data.database.WeatherDao
import com.example.skymood.data.database.WeatherEntity
import com.example.skymood.data.weather.model.WeatherResponse

import com.example.skymood.data.database.FavoriteEntity
import kotlinx.coroutines.flow.Flow

class WeatherLocalDataSource(private val weatherDao: WeatherDao) {

    suspend fun saveWeather(weatherResponse: WeatherResponse?) {
        val entity = WeatherEntity(weatherResponse = weatherResponse)
        weatherDao.insertWeather(entity)
    }

    suspend fun getCachedWeather(): WeatherResponse? {
        return weatherDao.getWeather()?.weatherResponse
    }

    suspend fun insertFavorite(favoriteEntity: FavoriteEntity) {
        weatherDao.insertFavorite(favoriteEntity)
    }

    suspend fun deleteFavorite(favoriteEntity: FavoriteEntity) {
        weatherDao.deleteFavorite(favoriteEntity)
    }

    fun getFavorites(): Flow<List<FavoriteEntity>> {
        return weatherDao.getFavorites()
    }
}
