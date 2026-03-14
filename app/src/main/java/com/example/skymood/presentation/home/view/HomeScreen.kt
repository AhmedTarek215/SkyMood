package com.example.skymood.presentation.home.view

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.skymood.presentation.home.LocationSelectionDialog
import com.example.skymood.presentation.home.WeatherContentView
import com.example.skymood.presentation.home.getBackgroundGradient
import com.example.skymood.presentation.home.viewmodel.HomeViewModel
import com.example.skymood.presentation.home.viewmodel.WeatherUiState
import com.example.skymood.presentation.home.viewmodel.WeatherUiEvent
import kotlinx.coroutines.flow.collectLatest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToMap: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val weatherData by viewModel.weatherData.collectAsState()
    val location by viewModel.location.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showLocationDialog by remember { mutableStateOf(false) }

    val gpsResolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.fetchGpsLocation()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is WeatherUiEvent.ShowError -> {
                    if (event.message == "RESOLUTION_REQUIRED") {
                        try {
                            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
                            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                            val settingsClient = LocationServices.getSettingsClient(context)
                            val responseTask = settingsClient.checkLocationSettings(builder.build())
                            responseTask.addOnFailureListener { exception ->
                                if (exception is ResolvableApiException) {
                                    try {
                                        gpsResolutionLauncher.launch(
                                            IntentSenderRequest.Builder(exception.resolution).build()
                                        )
                                    } catch (_: Exception) {}
                                }
                            }
                        } catch (_: Exception) {}
                    } else {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
                is WeatherUiEvent.ShowToast -> {
                     snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it }
        if (granted && location == null) {
            showLocationDialog = true
        }
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestOrShowDialog() {
        if (hasLocationPermission()) {
            showLocationDialog = true
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        if (location == null) {
            requestOrShowDialog()
        }
    }

    if (showLocationDialog) {
        LocationSelectionDialog(
            onDismiss = { showLocationDialog = false },
            onUseGps = {
                showLocationDialog = false
                viewModel.fetchGpsLocation()
            },
            onPickOnMap = {
                showLocationDialog = false
                onNavigateToMap()
            }
        )
    }

    val pullRefreshState = rememberPullToRefreshState()
    val currentCondition = weatherData?.list?.firstOrNull()?.weather?.firstOrNull()?.main ?: ""
    val dynamicBackground = remember(currentCondition) { getBackgroundGradient(currentCondition) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dynamicBackground)
    ) {
        PullToRefreshBox(
            isRefreshing = uiState is WeatherUiState.Loading,
            onRefresh = { viewModel.refreshData() },
            state = pullRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            Crossfade(targetState = uiState, label = "home_crossfade") { state ->
                when (state) {
                    is WeatherUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(color = Color(0xFF4FC3F7))
                                Text("Fetching weather…", color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }

                    is WeatherUiState.Error -> {
                        if (location == null) {
                             Box(
                                modifier = Modifier.fillMaxSize().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, null, tint = Color(0xFF4FC3F7), modifier = Modifier.size(48.dp))
                                    Text(state.message, color = Color.White, textAlign = TextAlign.Center)
                                    Button(
                                        onClick = { requestOrShowDialog() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                                    ) { Text("Try Again") }
                                }
                            }
                        } else if (weatherData != null) {
                            WeatherContentView(
                                weatherData = weatherData!!,
                                onChangeLocation = { requestOrShowDialog() },
                                pullRefreshState = pullRefreshState,
                                isOffline = true
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(state.message, color = Color.White, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = { viewModel.refreshData() }) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }

                    is WeatherUiState.Success -> {
                        WeatherContentView(
                            weatherData = state.data,
                            onChangeLocation = { requestOrShowDialog() },
                            pullRefreshState = pullRefreshState,
                            isOffline = state.isOffline
                        )
                    }
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun ScreenNoLocation(onSelect: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF4FC3F7), modifier = Modifier.size(64.dp))
            Text(
                "Set your location to view weather",
                color = Color.White, fontSize = 18.sp,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
            )
            Text("Use GPS or pick from the map", color = Color.White.copy(alpha = 0.6f))
            Button(
                onClick = { onSelect() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Select Location") }
        }
    }
}
