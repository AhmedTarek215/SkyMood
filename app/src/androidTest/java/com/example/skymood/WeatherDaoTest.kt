package com.example.skymood

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.skymood.data.database.AlertEntity
import com.example.skymood.data.database.FavoriteEntity
import com.example.skymood.data.database.WeatherDao
import com.example.skymood.data.database.WeatherDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeatherDaoTest {

    private lateinit var database: WeatherDatabase
    private lateinit var dao: WeatherDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.weatherDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetFavorite_returnsFavoriteInList() = runTest {
        // Given
        val favorite = FavoriteEntity(
            id = 1,
            cityName = "Cairo",
            countryName = "EG",
            lat = 30.0444,
            lon = 31.2357
        )

        // When
        dao.insertFavorite(favorite)

        // Then
        dao.getFavorites().test {
            val favorites = awaitItem()
            assertEquals(1, favorites.size)
            assertEquals("Cairo", favorites[0].cityName)
            assertEquals("EG", favorites[0].countryName)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun deleteFavorite_removesFromList() = runTest {
        // Given
        val favorite = FavoriteEntity(
            id = 1,
            cityName = "Cairo",
            countryName = "EG",
            lat = 30.0444,
            lon = 31.2357
        )
        dao.insertFavorite(favorite)

        // When
        dao.deleteFavorite(favorite)

        // Then
        dao.getFavorites().test {
            val favorites = awaitItem()
            assertTrue(favorites.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun insertAndGetAlert_returnsCorrectAlert() = runTest {
        // Given
        val alert = AlertEntity(
            id = 1,
            cityName = "Alexandria",
            lat = 31.2001,
            lon = 29.9187,
            alertType = "NOTIFICATION",
            startTimeMillis = 1000L,
            endTimeMillis = 2000L,
            isEnabled = true
        )

        // When
        val insertedId = dao.insertAlert(alert)

        // Then
        val retrieved = dao.getAlertById(insertedId.toInt())
        assertNotNull(retrieved)
        assertEquals("Alexandria", retrieved!!.cityName)
        assertEquals("NOTIFICATION", retrieved.alertType)
        assertTrue(retrieved.isEnabled)
    }
}
