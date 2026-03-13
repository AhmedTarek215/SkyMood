package com.example.skymood.data.weather.datasource.remote

import com.example.skymood.data.weather.model.GeocodingResponse
import com.example.skymood.data.weather.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): Response<WeatherResponse>

    @GET("geo/1.0/direct")
    suspend fun searchCity(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String
    ): Response<List<GeocodingResponse>>
}