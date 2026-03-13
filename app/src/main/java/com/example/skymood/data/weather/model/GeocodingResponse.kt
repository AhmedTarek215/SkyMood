package com.example.skymood.data.weather.model

data class GeocodingResponse(
    val name: String,
    val local_names: Map<String, String>?,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String?
)
