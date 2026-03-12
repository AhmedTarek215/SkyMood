package com.example.skymood.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.skymood.data.weather.model.WeatherResponse

@Entity(tableName = "weather_table")
data class WeatherEntity(
    @PrimaryKey
    val id: Int = 1, // Only store the last known location
    val weatherResponse: WeatherResponse?
)
