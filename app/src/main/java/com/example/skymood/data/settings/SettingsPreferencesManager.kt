package com.example.skymood.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsPreferencesManager(private val context: Context) {

    companion object {
        val LOCATION_METHOD = stringPreferencesKey("location_method")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val WIND_SPEED_UNIT = stringPreferencesKey("wind_speed_unit")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    val locationMethodStream: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LOCATION_METHOD] ?: "gps"
    }

    val temperatureUnitStream: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TEMPERATURE_UNIT] ?: "celsius"
    }

    val windSpeedUnitStream: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[WIND_SPEED_UNIT] ?: "mps"
    }

    val appLanguageStream: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_LANGUAGE] ?: "en"
    }

    suspend fun saveLocationMethod(method: String) {
        context.dataStore.edit { preferences ->
            preferences[LOCATION_METHOD] = method
        }
    }

    suspend fun saveTemperatureUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[TEMPERATURE_UNIT] = unit
        }
    }

    suspend fun saveWindSpeedUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[WIND_SPEED_UNIT] = unit
        }
    }

    suspend fun saveAppLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_LANGUAGE] = lang
        }
    }
}
