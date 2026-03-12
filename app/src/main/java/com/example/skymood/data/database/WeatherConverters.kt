package com.example.skymood.data.database

import androidx.room.TypeConverter
import com.example.skymood.data.weather.model.WeatherResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WeatherConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromWeatherResponse(weatherResponse: WeatherResponse?): String? {
        if (weatherResponse == null) return null
        return gson.toJson(weatherResponse)
    }

    @TypeConverter
    fun toWeatherResponse(weatherResponseString: String?): WeatherResponse? {
        if (weatherResponseString == null) return null
        val type = object : TypeToken<WeatherResponse>() {}.type
        return gson.fromJson(weatherResponseString, type)
    }
}
