package com.example.skymood.data.weather.datasource.local

import com.example.skymood.data.database.WeatherDao
import com.example.skymood.data.database.WeatherEntity
import com.example.skymood.data.weather.model.WeatherResponse

class WeatherLocalDataSource(private val weatherDao: WeatherDao) {

    suspend fun saveWeather(weatherResponse: WeatherResponse?) {
        val entity = WeatherEntity(weatherResponse = weatherResponse)
        weatherDao.insertWeather(entity)
    }

    suspend fun getCachedWeather(): WeatherResponse? {
        return weatherDao.getWeather()?.weatherResponse
    }
}
