package com.example.skymood.presentation.favorites.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.skymood.R
import com.example.skymood.presentation.favorites.viewmodel.FavoriteForecastViewModel
import com.example.skymood.presentation.home.WeatherContentView
import com.example.skymood.presentation.home.getBackgroundGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteForecastScreen(
    lat: Double,
    lon: Double,
    viewModel: FavoriteForecastViewModel,
    onBack: () -> Unit
) {
    val weatherData by viewModel.weatherData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val windUnit by viewModel.windUnit.collectAsState()
    val temperatureUnit by viewModel.temperatureUnit.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchForecast(lat, lon)
    }

    val pullRefreshState = rememberPullToRefreshState()
    val bgMain = weatherData?.list?.firstOrNull()?.weather?.firstOrNull()?.main
    val backgroundBrush = remember(bgMain) { getBackgroundGradient(bgMain) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favorites_forecast_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.favorites_forecast_back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(backgroundBrush)
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refreshData() },
            state = pullRefreshState,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            when {
                isLoading && weatherData == null && !pullRefreshState.isAnimating -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4FC3F7))
                    }
                }
                errorMessage != null && weatherData == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(Icons.Default.Refresh, null, tint = Color(0xFF4FC3F7), modifier = Modifier.size(48.dp))
                            Text(errorMessage ?: stringResource(R.string.favorites_offline_message), color = Color.White, textAlign = TextAlign.Center)
                            Button(
                                onClick = { viewModel.fetchForecast(lat, lon) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                            ) { Text(stringResource(R.string.home_retry)) }
                        }
                    }
                }
                weatherData != null -> {
                    val tempUnitText = when (temperatureUnit) {
                        "fahrenheit" -> "°F"
                        "kelvin" -> "K"
                        else -> "°C"
                    }
                    WeatherContentView(
                        weatherData = weatherData!!,
                        onChangeLocation = {}, 
                        pullRefreshState = pullRefreshState,
                        isOffline = false,
                        showCurrentLocationLabel = false,
                        windUnitString = windUnit,
                        temperatureUnitString = tempUnitText
                    )
                }
            }
        }
    }
}
