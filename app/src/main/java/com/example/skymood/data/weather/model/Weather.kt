package com.example.skymood.data.weather.model

data class WeatherResponse(
    val list: List<ForecastItem>,
    val city: City
)

data class City(
    val name: String,
    val country: String,
    val timezone: Int
)

data class ForecastItem(
    val main: MainData,
    val weather: List<WeatherDescription>,
    val wind: WindData,
    val clouds: CloudsData,
    val dt_txt: String
)

data class MainData(
    val temp: Double,
    val feels_like: Double,
    val pressure: Int,
    val humidity: Int
)

data class WeatherDescription(
    val main: String,
    val description: String,
    val icon: String
)

data class WindData(
    val speed: Double
)

data class CloudsData(
    val all: Int
)