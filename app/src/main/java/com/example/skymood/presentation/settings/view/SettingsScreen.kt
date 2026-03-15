package com.example.skymood.presentation.settings.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skymood.R
import com.example.skymood.presentation.settings.viewmodel.SettingsViewModel
import com.example.skymood.utils.NetworkUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val locationMethod by viewModel.locationMethod.collectAsState()
    val temperatureUnit by viewModel.temperatureUnit.collectAsState()
    val windSpeedUnit by viewModel.windSpeedUnit.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val onAction = { action: () -> Unit ->
        if (NetworkUtils.isNetworkAvailable(context)) {
            action()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.home_offline_warning))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFF0C1623) // Main background color for settings
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Location Section
            SettingsSection(
                title = stringResource(R.string.settings_location),
                iconResId = R.drawable.ic_location_blue,
                options = listOf(
                    SettingsOption(stringResource(R.string.settings_gps), "gps"),
                    SettingsOption(stringResource(R.string.settings_map), "map")
                ),
                selectedOption = locationMethod,
                onOptionSelected = { onAction { viewModel.setLocationMethod(it) } }
            )

            // Temperature Units Section
            SettingsSection(
                title = stringResource(R.string.settings_temperature_units),
                iconResId = R.drawable.ic_temprature_blue,
                options = listOf(
                    SettingsOption(stringResource(R.string.settings_celsius), "celsius"),
                    SettingsOption(stringResource(R.string.settings_fahrenheit), "fahrenheit"),
                    SettingsOption(stringResource(R.string.settings_kelvin), "kelvin")
                ),
                selectedOption = temperatureUnit,
                onOptionSelected = { onAction { viewModel.setTemperatureUnit(it) } }
            )

            // Wind Speed Section
            SettingsSection(
                title = stringResource(R.string.settings_wind_speed),
                iconResId = R.drawable.ic_wind_blue,
                options = listOf(
                    SettingsOption(stringResource(R.string.settings_mps), "mps"),
                    SettingsOption(stringResource(R.string.settings_mph), "mph")
                ),
                selectedOption = windSpeedUnit,
                onOptionSelected = { onAction { viewModel.setWindSpeedUnit(it) } }
            )

            // App Language Section
            SettingsSection(
                title = stringResource(R.string.settings_app_language),
                iconResId = R.drawable.ic_language_blue,
                options = listOf(
                    SettingsOption(stringResource(R.string.settings_english), "en", stringResource(R.string.settings_english_desc)),
                    SettingsOption(stringResource(R.string.settings_arabic), "ar", stringResource(R.string.settings_arabic_desc))
                ),
                selectedOption = appLanguage,
                onOptionSelected = { onAction { viewModel.setAppLanguage(it) } }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

data class SettingsOption(val label: String, val value: String, val subLabel: String? = null)

@Composable
fun SettingsSection(
    title: String,
    iconResId: Int,
    options: List<SettingsOption>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = Color(0xFF4FC3F7),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        options.forEachIndexed { index, option ->
            val isFirst = index == 0
            val isLast = index == options.size - 1

            val shape = when {
                options.size == 1 -> RoundedCornerShape(12.dp)
                isFirst -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                isLast -> RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                else -> RoundedCornerShape(0.dp)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isLast) 0.dp else 8.dp)
                    .background(Color(0xFF1D2833), shape)
                    .clickable { onOptionSelected(option.value) }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = option.label,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (option.subLabel != null) {
                        Text(
                            text = option.subLabel,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                RadioButton(
                    selected = selectedOption == option.value,
                    onClick = { onOptionSelected(option.value) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF4FC3F7),
                        unselectedColor = Color.Gray
                    )
                )
            }
        }
    }
}
