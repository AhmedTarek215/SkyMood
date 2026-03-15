package com.example.skymood

import app.cash.turbine.test
import com.example.skymood.data.database.FavoriteEntity
import com.example.skymood.data.weather.WeatherRepository
import com.example.skymood.data.weather.datasource.local.WeatherLocalDataSource
import com.example.skymood.data.weather.datasource.remote.WeatherRemoteDataSource
import com.example.skymood.data.weather.model.City
import com.example.skymood.data.weather.model.WeatherResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherRepositoryTest {

    private lateinit var remoteDataSource: WeatherRemoteDataSource
    private lateinit var localDataSource: WeatherLocalDataSource
    private lateinit var repository: WeatherRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        remoteDataSource = mockk(relaxed = true)
        localDataSource = mockk(relaxed = true)
        repository = WeatherRepository(remoteDataSource, localDataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchWeatherFlow emits remote data on success`() = runTest {
        // Given
        val weatherResponse = WeatherResponse(
            list = emptyList(),
            city = City(name = "Cairo", country = "EG", timezone = 7200)
        )
        coEvery {
            remoteDataSource.getForecast(any(), any(), any(), any(), any())
        } returns weatherResponse

        // When & Then
        repository.fetchWeatherFlow(30.0, 31.0, "key", "metric", "en", true).test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals("Cairo", result!!.city.name)
            awaitComplete()
        }
    }

    @Test
    fun `fetchWeatherFlow falls back to cache on network error`() = runTest {
        // Given
        val cachedResponse = WeatherResponse(
            list = emptyList(),
            city = City(name = "CachedCity", country = "EG", timezone = 7200)
        )
        coEvery {
            remoteDataSource.getForecast(any(), any(), any(), any(), any())
        } throws Exception("Network error")
        coEvery { localDataSource.getCachedWeather() } returns cachedResponse

        // When & Then
        repository.fetchWeatherFlow(30.0, 31.0, "key", "metric", "en", true).test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals("CachedCity", result!!.city.name)
            awaitComplete()
        }
    }

    @Test
    fun `insertFavorite delegates to localDataSource`() = runTest {
        // Given
        val favorite = FavoriteEntity(
            id = 0,
            cityName = "Alexandria",
            countryName = "EG",
            lat = 31.2001,
            lon = 29.9187
        )

        // When
        repository.insertFavorite(favorite)

        // Then
        coVerify { localDataSource.insertFavorite(favorite) }
    }
}
