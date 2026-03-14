package com.example.skymood.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weatherEntity: WeatherEntity)

    @Query("SELECT * FROM weather_table WHERE id = 1")
    suspend fun getWeather(): WeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favoriteEntity: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favoriteEntity: FavoriteEntity)

    @Query("SELECT * FROM favorites_table")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    // Alert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alertEntity: AlertEntity): Long

    @Delete
    suspend fun deleteAlert(alertEntity: AlertEntity)

    @Query("DELETE FROM alerts_table WHERE id = :id")
    suspend fun deleteAlertById(id: Int)

    @Query("UPDATE alerts_table SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateAlertEnabled(id: Int, isEnabled: Boolean)

    @Query("SELECT * FROM alerts_table")
    fun getAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts_table WHERE id = :id")
    suspend fun getAlertById(id: Int): AlertEntity?
}
