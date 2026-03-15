package com.example.skymood

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.skymood.data.settings.SettingsPreferencesManager
import com.example.skymood.data.weather.WeatherRepository
import com.example.skymood.data.weather.model.City
import com.example.skymood.data.weather.model.WeatherResponse
import com.example.skymood.presentation.home.viewmodel.HomeViewModel
import com.example.skymood.presentation.home.viewmodel.WeatherUiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: WeatherRepository
    private lateinit var preferencesManager: SettingsPreferencesManager
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        preferencesManager = mockk(relaxed = true)

        // Mock preferences streams
        every { preferencesManager.temperatureUnitStream } returns flowOf("celsius")
        every { preferencesManager.windSpeedUnitStream } returns flowOf("mps")
        every { preferencesManager.appLanguageStream } returns flowOf("en")

        // Mock repository weatherState
        every { repository.weatherState } returns MutableStateFlow(null)

        val application = RuntimeEnvironment.getApplication() as Application
        viewModel = HomeViewModel(repository, preferencesManager, application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setLocation updates location StateFlow`() = runTest {
        // When
        viewModel.setLocation(30.0, 31.0)

        // Then
        val location = viewModel.location.value
        assertNotNull(location)
        assertEquals(30.0, location!!.first, 0.001)
        assertEquals(31.0, location.second, 0.001)
    }

    @Test
    fun `setLocation triggers weather fetch and updates uiState`() = runTest {
        // Given
        val weatherResponse = WeatherResponse(
            list = emptyList(),
            city = City(name = "Cairo", country = "EG", timezone = 7200)
        )
        coEvery {
            repository.fetchWeatherFlow(any(), any(), any(), any(), any(), any())
        } returns flowOf(weatherResponse)

        // When
        viewModel.setLocation(30.0, 31.0)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue("Expected Success state but got $state", state is WeatherUiState.Success)
        assertEquals("Cairo", (state as WeatherUiState.Success).data.city.name)
    }

    @Test
    fun `refreshData with existing location fetches weather again`() = runTest {
        // Given
        val weatherResponse = WeatherResponse(
            list = emptyList(),
            city = City(name = "Giza", country = "EG", timezone = 7200)
        )
        coEvery {
            repository.fetchWeatherFlow(any(), any(), any(), any(), any(), any())
        } returns flowOf(weatherResponse)

        // Set initial location
        viewModel.setLocation(29.9, 31.2)
        advanceUntilIdle()

        // When — refresh data
        val updatedResponse = WeatherResponse(
            list = emptyList(),
            city = City(name = "Giza Updated", country = "EG", timezone = 7200)
        )
        coEvery {
            repository.fetchWeatherFlow(any(), any(), any(), any(), any(), any())
        } returns flowOf(updatedResponse)

        viewModel.refreshData()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Success)
        assertEquals("Giza Updated", (state as WeatherUiState.Success).data.city.name)
    }
}
