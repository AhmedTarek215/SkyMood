package com.example.skymood

import com.example.skymood.data.database.FavoriteEntity
import com.example.skymood.data.database.WeatherDao
import com.example.skymood.data.database.WeatherEntity
import com.example.skymood.data.weather.datasource.local.WeatherLocalDataSource
import com.example.skymood.data.weather.model.City
import com.example.skymood.data.weather.model.WeatherResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WeatherLocalDataSourceTest {

    private lateinit var dao: WeatherDao
    private lateinit var localDataSource: WeatherLocalDataSource

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        localDataSource = WeatherLocalDataSource(dao)
    }

    @Test
    fun `saveWeather calls dao insertWeather with correct entity`() = runTest {
        // Given
        val weatherResponse = WeatherResponse(
            list = emptyList(),
            city = City(name = "Cairo", country = "EG", timezone = 7200)
        )

        // When
        localDataSource.saveWeather(weatherResponse)

        // Then
        coVerify {
            dao.insertWeather(
                match { entity ->
                    entity.weatherResponse?.city?.name == "Cairo"
                }
            )
        }
    }

    @Test
    fun `getCachedWeather returns weather response from dao`() = runTest {
        // Given
        val weatherResponse = WeatherResponse(
            list = emptyList(),
            city = City(name = "Giza", country = "EG", timezone = 7200)
        )
        val entity = WeatherEntity(id = 1, weatherResponse = weatherResponse)
        coEvery { dao.getWeather() } returns entity

        // When
        val result = localDataSource.getCachedWeather()

        // Then
        assertNotNull(result)
        assertEquals("Giza", result!!.city.name)
    }

    @Test
    fun `getCachedWeather returns null when dao returns null`() = runTest {
        // Given
        coEvery { dao.getWeather() } returns null

        // When
        val result = localDataSource.getCachedWeather()

        // Then
        assertNull(result)
    }

    @Test
    fun `insertFavorite delegates to dao`() = runTest {
        // Given
        val favorite = FavoriteEntity(
            id = 0,
            cityName = "Alexandria",
            countryName = "EG",
            lat = 31.2001,
            lon = 29.9187
        )

        // When
        localDataSource.insertFavorite(favorite)

        // Then
        coVerify { dao.insertFavorite(favorite) }
    }
}
