package com.example.skymood.presentation.weatheralerts.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.skymood.data.database.AlertEntity
import com.example.skymood.data.weather.AlertScheduler
import com.example.skymood.data.weather.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WeatherAlertsViewModel(
    private val repository: WeatherRepository,
    private val application: Application
) : AndroidViewModel(application) {

    val alerts: StateFlow<List<AlertEntity>> = repository.getAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentLocation = MutableStateFlow<Triple<Double, Double, String>?>(null)
    val currentLocation: StateFlow<Triple<Double, Double, String>?> = _currentLocation

    fun setCurrentLocation(lat: Double, lon: Double, cityName: String) {
        _currentLocation.value = Triple(lat, lon, cityName)
    }

    fun addAlert(
        lat: Double,
        lon: Double,
        cityName: String,
        alertType: String,
        startTimeMillis: Long,
        endTimeMillis: Long
    ) {
        viewModelScope.launch {
            val alert = AlertEntity(
                cityName = cityName,
                lat = lat,
                lon = lon,
                alertType = alertType,
                startTimeMillis = startTimeMillis,
                endTimeMillis = endTimeMillis,
                isEnabled = true
            )
            val id = repository.insertAlert(alert)
            val savedAlert = alert.copy(id = id.toInt())
            AlertScheduler.scheduleAlert(application, savedAlert)
        }
    }

    fun deleteAlert(alert: AlertEntity) {
        viewModelScope.launch {
            AlertScheduler.cancelAlert(application, alert.id)
            repository.deleteAlert(alert)
        }
    }

    fun toggleAlert(alertId: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.updateAlertEnabled(alertId, isEnabled)
            if (!isEnabled) {
                AlertScheduler.cancelAlert(application, alertId)
            } else {
                val alert = repository.getAlertById(alertId)
                if (alert != null && alert.startTimeMillis > System.currentTimeMillis()) {
                    AlertScheduler.scheduleAlert(application, alert)
                }
            }
        }
    }

    fun cleanupExpiredAlerts() {
        viewModelScope.launch {
            val allAlerts = alerts.value
            val now = System.currentTimeMillis()
            allAlerts.filter { it.endTimeMillis < now }.forEach { alert ->
                AlertScheduler.cancelAlert(application, alert.id)
                repository.deleteAlert(alert)
            }
        }
    }
}

class WeatherAlertsViewModelFactory(
    private val repository: WeatherRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return WeatherAlertsViewModel(repository, application) as T
    }
}
