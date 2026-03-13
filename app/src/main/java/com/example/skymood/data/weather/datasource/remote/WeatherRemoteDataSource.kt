package com.example.skymood.data.weather.datasource.remote

import com.example.skymood.data.network.Network
import com.example.skymood.data.weather.model.GeocodingResponse
import com.example.skymood.data.weather.model.WeatherResponse
import retrofit2.Response

class WeatherRemoteDataSource {
    private val weatherService = Network.weatherService

    suspend fun getForecast(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String
    ): WeatherResponse? {
        val response = weatherService.getForecast(lat, lon, apiKey, units, lang)

        if (response.isSuccessful) {
            return response.body()
        } else {
            throw Exception("Error: ${response.code()} ${response.message()}")
        }
    }

    suspend fun searchCity(query: String, apiKey: String): List<GeocodingResponse>? {
        val response = weatherService.searchCity(query = query, apiKey = apiKey)
        if (response.isSuccessful) {
            return response.body()
        } else {
            throw Exception("Error: ${response.code()} ${response.message()}")
        }
    }
}