package com.example.skymood

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.skymood.data.database.FavoriteEntity
import com.example.skymood.data.settings.SettingsPreferencesManager
import com.example.skymood.data.weather.WeatherRepository
import com.example.skymood.presentation.favorites.viewmodel.FavoritesViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: WeatherRepository
    private lateinit var preferencesManager: SettingsPreferencesManager
    private lateinit var viewModel: FavoritesViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        preferencesManager = mockk(relaxed = true)

        val favList = listOf(
            FavoriteEntity(id = 1, cityName = "Cairo", countryName = "EG", lat = 30.0, lon = 31.0),
            FavoriteEntity(id = 2, cityName = "Giza", countryName = "EG", lat = 29.9, lon = 31.2)
        )
        every { repository.getFavorites() } returns flowOf(favList)

        viewModel = FavoritesViewModel(repository, preferencesManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addFavorite calls repository insertFavorite`() = runTest {
        // Given
        val favorite = FavoriteEntity(
            id = 0,
            cityName = "Alexandria",
            countryName = "EG",
            lat = 31.2001,
            lon = 29.9187
        )

        // When
        viewModel.addFavorite(favorite)
        advanceUntilIdle()

        // Then
        coVerify { repository.insertFavorite(favorite) }
    }

    @Test
    fun `removeFavorite calls repository deleteFavorite`() = runTest {
        // Given
        val favorite = FavoriteEntity(
            id = 1,
            cityName = "Cairo",
            countryName = "EG",
            lat = 30.0444,
            lon = 31.2357
        )

        // When
        viewModel.removeFavorite(favorite)
        advanceUntilIdle()

        // Then
        coVerify { repository.deleteFavorite(favorite) }
    }

    @Test
    fun `favorites StateFlow emits repository data`() = runTest(testDispatcher) {
        // Start collecting to activate stateIn
        val job = launch { viewModel.favorites.collect {} }

        // Then
        val favorites = viewModel.favorites.value
        assertEquals(2, favorites.size)
        assertEquals("Cairo", favorites[0].cityName)
        assertEquals("Giza", favorites[1].cityName)

        job.cancel()
    }
}
