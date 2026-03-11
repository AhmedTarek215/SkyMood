package com.example.skymood.presentation.home.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.skymood.data.weather.WeatherRepository
import com.example.skymood.data.weather.model.WeatherResponse
import com.example.skymood.utils.Constants
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel(
    private val repository: WeatherRepository,
    application: Application
) : AndroidViewModel(application) {

    val weatherData: StateFlow<WeatherResponse?> = repository.weatherState

    private val _location = MutableStateFlow<Pair<Double, Double>?>(null)
    val location: StateFlow<Pair<Double, Double>?> = _location

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun setLocation(lat: Double, lon: Double) {
        _location.value = Pair(lat, lon)
        fetchWeather(lat, lon)
    }

    @SuppressLint("MissingPermission")
    fun fetchGpsLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val locationRequest = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
                val builder = com.google.android.gms.location.LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                val settingsClient = LocationServices.getSettingsClient(getApplication<Application>())
                
                try {
                    settingsClient.checkLocationSettings(builder.build()).await()
                    val fusedClient = LocationServices.getFusedLocationProviderClient(getApplication<Application>())
                    val cts = CancellationTokenSource()
                    val location = fusedClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cts.token
                    ).await()
                    if (location != null) {
                        _location.value = Pair(location.latitude, location.longitude)
                        fetchWeather(location.latitude, location.longitude)
                    } else {
                        val last = fusedClient.lastLocation.await()
                        if (last != null) {
                            _location.value = Pair(last.latitude, last.longitude)
                            fetchWeather(last.latitude, last.longitude)
                        } else {
                            _isLoading.value = false
                            _errorMessage.value = "Could not get GPS location. Please try again outside."
                        }
                    }
                } catch (e: Exception) {
                    _isLoading.value = false
                    if (e is com.google.android.gms.common.api.ResolvableApiException) {
                        _errorMessage.value = "RESOLUTION_REQUIRED"
                    } else {
                        _errorMessage.value = "GPS settings error: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "GPS error: ${e.message}"
            }
        }
    }

    fun refreshData() {
        val loc = _location.value
        if (loc != null) {
            fetchWeather(loc.first, loc.second)
        } else {
            fetchGpsLocation()
        }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.fetchWeather(lat, lon, Constants.WEATHER_API_KEY, "metric", "en")
            } catch (e: Exception) {
                _errorMessage.value = "Weather fetch error: ${e.message}"
            }
            _isLoading.value = false
        }
    }
}

class HomeViewModelFactory(
    private val repository: WeatherRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(repository, application) as T
    }
}
