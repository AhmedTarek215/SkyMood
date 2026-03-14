package com.example.skymood.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts_table")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cityName: String,
    val lat: Double,
    val lon: Double,
    val alertType: String, // "NOTIFICATION" or "ALARM"
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isEnabled: Boolean = true
)
