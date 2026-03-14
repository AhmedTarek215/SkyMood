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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.skymood.data.settings.SettingsPreferencesManager
import kotlinx.coroutines.flow.catch

class HomeViewModel(
    private val repository: WeatherRepository,
    private val preferencesManager: SettingsPreferencesManager,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<WeatherUiEvent>()
    val uiEvent: SharedFlow<WeatherUiEvent> = _uiEvent.asSharedFlow()

    val weatherData: StateFlow<WeatherResponse?> = repository.weatherState

    private val _location = MutableStateFlow<Pair<Double, Double>?>(null)
    val location: StateFlow<Pair<Double, Double>?> = _location

    private val _isCurrentLocationHome = MutableStateFlow(true)

    fun setLocation(lat: Double, lon: Double, isHomeLocation: Boolean = true) {
        _location.value = Pair(lat, lon)
        _isCurrentLocationHome.value = isHomeLocation
        fetchWeather(lat, lon, isHomeLocation)
    }

    @SuppressLint("MissingPermission")
    fun fetchGpsLocation() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
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
                        _isCurrentLocationHome.value = true
                        fetchWeather(location.latitude, location.longitude, true)
                    } else {
                        val last = fusedClient.lastLocation.await()
                        if (last != null) {
                            _location.value = Pair(last.latitude, last.longitude)
                            _isCurrentLocationHome.value = true
                            fetchWeather(last.latitude, last.longitude, true)
                        } else {
                            _uiState.value = WeatherUiState.Error("Could not get GPS location. Please try again outside.")
                            _uiEvent.emit(WeatherUiEvent.ShowError("Could not get GPS location. Please try again outside."))
                        }
                    }
                } catch (e: Exception) {
                    if (e is com.google.android.gms.common.api.ResolvableApiException) {
                        _uiEvent.emit(WeatherUiEvent.ShowError("RESOLUTION_REQUIRED"))
                    } else {
                        _uiState.value = WeatherUiState.Error("GPS settings error: ${e.message}")
                        _uiEvent.emit(WeatherUiEvent.ShowError("GPS settings error: ${e.message}"))
                    }
                }
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("GPS error: ${e.message}")
                _uiEvent.emit(WeatherUiEvent.ShowError("GPS error: ${e.message}"))
            }
        }
    }

    fun refreshData() {
        val loc = _location.value
        if (loc != null) {
            fetchWeather(loc.first, loc.second, _isCurrentLocationHome.value)
        } else {
            fetchGpsLocation()
        }
    }

    private fun fetchWeather(lat: Double, lon: Double, isHomeLocation: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val tempPref = preferencesManager.temperatureUnitStream.first()
                val langPref = preferencesManager.appLanguageStream.first()
                
                val unitParam = when (tempPref) {
                    "celsius" -> "metric"
                    "fahrenheit" -> "imperial"
                    else -> "standard"
                }

                repository.fetchWeatherFlow(lat, lon, Constants.WEATHER_API_KEY, unitParam, langPref, isHomeLocation)
                    .catch { e ->
                        _uiState.value = WeatherUiState.Error(e.message ?: "Unknown Error")
                        _uiEvent.emit(WeatherUiEvent.ShowError(e.message ?: "Unknown Error"))
                    }
                    .collectLatest { response ->
                        if (response != null) {
                            _uiState.value = WeatherUiState.Success(response, isOffline = false) // isOffline logic can be improved if repository returns it
                        } else {
                            _uiState.value = WeatherUiState.Error("No data found")
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("Weather fetch error: ${e.message}")
                _uiEvent.emit(WeatherUiEvent.ShowError("Weather fetch error: ${e.message}"))
            }
        }
    }
}

class HomeViewModelFactory(
    private val repository: WeatherRepository,
    private val application: Application,
    private val preferencesManager: SettingsPreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(repository, preferencesManager, application) as T
    }
}
