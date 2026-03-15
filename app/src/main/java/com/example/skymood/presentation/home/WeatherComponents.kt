package com.example.skymood.presentation.home

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skymood.R
import com.example.skymood.data.weather.model.WeatherResponse
import java.text.SimpleDateFormat
import java.util.*

fun getBackgroundGradient(weatherMain: String?): Brush {
    val colors = when (weatherMain?.lowercase()) {
        "clear" -> listOf(Color(0xFF1D3349), Color(0xFF0C1623))
        "clouds" -> listOf(Color(0xFF2C3E50), Color(0xFF1B2838))
        "rain", "drizzle" -> listOf(Color(0xFF1F2833), Color(0xFF0F1419))
        "thunderstorm" -> listOf(Color(0xFF111E2D), Color(0xFF000000))
        "snow" -> listOf(Color(0xFF3E4A59), Color(0xFF1A1F25))
        "mist", "fog", "haze", "smoke", "dust" -> listOf(Color(0xFF2A3A4A), Color(0xFF141D26))
        else -> listOf(Color(0xFF1D3349), Color(0xFF0C1623))
    }
    return Brush.verticalGradient(colors)
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun WeatherContentView(
    weatherData: WeatherResponse,
    windUnitString: String,
    temperatureUnitString: String,
    onChangeLocation: () -> Unit,
    pullRefreshState: androidx.compose.material3.pulltorefresh.PullToRefreshState,
    isOffline: Boolean,
    showCurrentLocationLabel: Boolean = true
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val offlineWarningText = stringResource(R.string.home_offline_warning)
    LaunchedEffect(isOffline) {
        if (isOffline) {
            snackbarHostState.showSnackbar(
                message = offlineWarningText,
                duration = SnackbarDuration.Long
            )
        }
    }
    val currentForecast = weatherData.list.firstOrNull()
    val cityName = weatherData.city.name
    val temp = currentForecast?.main?.temp?.toInt() ?: 0
    val humidity = currentForecast?.main?.humidity ?: 0
    val windSpeed = currentForecast?.wind?.speed ?: 0.0
    val pressure = currentForecast?.main?.pressure ?: 0
    val clouds = currentForecast?.clouds?.all ?: 0
    val descriptionMain = currentForecast?.weather?.firstOrNull()?.main ?: "N/A"
    val descriptionText = currentForecast?.weather?.firstOrNull()?.description
        ?.split(" ")?.joinToString(" ") { it.replaceFirstChar { it.uppercase() } }
        ?: descriptionMain
    val feelsLike = currentForecast?.main?.feels_like?.toInt() ?: 0
    val dateText = currentForecast?.dt_txt?.let { formatDisplayDate(it) } ?: ""

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 40.dp, bottom = 24.dp)
        ) {
            Text(cityName, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
            if (showCurrentLocationLabel) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onChangeLocation() }
                        .padding(4.dp)
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF4FC3F7), modifier = Modifier.size(14.dp))
                    Text(" " + stringResource(R.string.home_current_location), fontSize = 10.sp, color = Color(0xFF4FC3F7), letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("$temp$temperatureUnitString", fontSize = 80.sp, color = Color.White, fontWeight = FontWeight.ExtraBold, letterSpacing = (-2).sp)
            Text(dateText, fontSize = 16.sp, color = Color.White.copy(alpha = 0.9f))
            
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = Color(0xFF1E2836).copy(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(vertical = 4.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Image(
                        painter = painterResource(id = getWeatherIcon(descriptionMain)),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(descriptionText, fontSize = 15.sp, color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.home_feels_like, feelsLike), fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))

            Spacer(modifier = Modifier.height(16.dp))

            WeatherDetailsCard(
                humidity = "$humidity%",
                wind = "${"%.0f".format(windSpeed)} $windUnitString",
                pressure = "$pressure hPa",
                clouds = "$clouds%"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                stringResource(R.string.home_hourly_forecast),
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp
            )

            val hourlyList = remember(weatherData.list) {
                val list = mutableListOf<Triple<String, String, Int>>()
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                
                for (i in 0 until weatherData.list.size - 1) {
                    val current = weatherData.list[i]
                    val next = weatherData.list[i+1]
                    val currentTime = inputFormat.parse(current.dt_txt) ?: continue
                    
                    list.add(Triple(
                        formatHourFromDtTxt(current.dt_txt),
                        "${current.main.temp.toInt()}°",
                        getWeatherIcon(current.weather.firstOrNull()?.main ?: "")
                    ))
                    
                    for (hourOffset in 1..2) {
                        val interpTemp = current.main.temp + (next.main.temp - current.main.temp) * (hourOffset / 3.0)
                        val cal = Calendar.getInstance().apply { 
                            time = currentTime
                            add(Calendar.HOUR_OF_DAY, hourOffset) 
                        }
                        val hourLabel = SimpleDateFormat("h a", Locale.getDefault()).format(cal.time)
                        val icon = if (hourOffset == 1) {
                            getWeatherIcon(current.weather.firstOrNull()?.main ?: "")
                        } else {
                            getWeatherIcon(next.weather.firstOrNull()?.main ?: "")
                        }
                        list.add(Triple(hourLabel, "${interpTemp.toInt()}°", icon))
                    }
                    if (list.size >= 24) break
                }
                list
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                hourlyList.forEachIndexed { index, triple ->
                    val (time, t, icon) = triple
                    HourlyItem(
                        time = time,
                        temp = t,
                        iconRes = icon,
                        isSelected = index == 0
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                stringResource(R.string.home_5_day_forecast),
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp
            )

            val dailyForecasts = remember(weatherData.list) {
                val groups = weatherData.list
                    .groupBy { it.dt_txt.substring(0, 10) }
                    .entries.toList()
                val startFrom = if (groups.size > 5) 1 else 0
                groups.drop(startFrom).take(5).map { (date, items) ->
                    val rep = items.find { it.dt_txt.contains("12:00:00") } ?: items[items.size / 2]
                    val minT = items.minOf { it.main.temp }.toInt()
                    val maxT = items.maxOf { it.main.temp }.toInt()
                    Triple(date, rep, maxT to minT)
                }
            }

            val strToday = stringResource(R.string.day_today)
            val strTomorrow = stringResource(R.string.day_tomorrow)

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                dailyForecasts.forEach { triple ->
                    val (date, forecast, temps) = triple
                    val desc = forecast.weather.firstOrNull()?.description
                        ?.split(" ")?.joinToString(" ") { it.replaceFirstChar { it.uppercase() } }
                        ?: (forecast.weather.firstOrNull()?.main ?: "")
                        
                    DailyItem(
                        dayName = formatDayName(date, strToday, strTomorrow),
                        dateLabel = formatShortDate(date),
                        description = desc,
                        highTemp = temps.first,
                        lowTemp = temps.second,
                        iconRes = getWeatherIconBlue(forecast.weather.firstOrNull()?.main ?: "")
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}


@Composable
fun WeatherDetailsCard(humidity: String, wind: String, pressure: String, clouds: String) {
    Surface(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        color = Color(0xFF1E2836).copy(alpha = 0.4f), 
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailItem(stringResource(R.string.weather_humidity), humidity, R.drawable.ic_humidity_blue, Modifier.weight(1f))
                DetailItem(stringResource(R.string.weather_wind),     wind,     R.drawable.ic_wind_blue, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailItem(stringResource(R.string.weather_pressure), pressure, R.drawable.ic_pressure_blue, Modifier.weight(1f))
                DetailItem(stringResource(R.string.weather_clouds),   clouds,   R.drawable.ic_clouds_blue, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, iconRes: Int, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(Color(0xFF213555).copy(alpha = 0.9f), RoundedCornerShape(14.dp)), 
            contentAlignment = Alignment.Center
        ) {
            Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold)
            Text(value, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HourlyItem(time: String, temp: String, iconRes: Int, isSelected: Boolean) {
    Surface(
        color = if (isSelected) Color(0xFF1E88E5) else Color(0xFF1E2836).copy(alpha = 0.5f),
        shape = RoundedCornerShape(20.dp),
        border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.width(80.dp).height(140.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = time, 
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f), 
                fontSize = 13.sp, 
                fontWeight = FontWeight.Bold
            )
            Image(
                painter = painterResource(id = iconRes), 
                contentDescription = null, 
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = temp, 
                color = Color.White, 
                fontWeight = FontWeight.ExtraBold, 
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun DailyItem(dayName: String, dateLabel: String, description: String, highTemp: Int, lowTemp: Int, iconRes: Int) {
    Surface(
        color = Color(0xFF1E2836).copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(dayName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(dateLabel, color = Color.Gray, fontSize = 12.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                modifier = Modifier.weight(1.3f),
                horizontalArrangement = Arrangement.Start
            ) {
                Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(30.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Text(description, color = Color.White.copy(alpha = 0.8f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            Row(modifier = Modifier.weight(0.7f), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "${highTemp}°",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = " / ${lowTemp}°",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 16.sp
                )
            }
        }
    }
}


fun formatDisplayDate(dtTxt: String): String = try {
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        .parse(dtTxt)?.let { SimpleDateFormat("EEEE, dd MMM | hh:mm a", Locale.getDefault()).format(it) } ?: dtTxt
} catch (_: Exception) { dtTxt }

fun formatHourFromDtTxt(dtTxt: String): String = try {
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        .parse(dtTxt)?.let { SimpleDateFormat("h a", Locale.getDefault()).format(it) } ?: ""
} catch (_: Exception) { "" }

fun formatDayName(dateStr: String, strToday: String, strTomorrow: String): String = try {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(dateStr)
    val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.time
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.time
    
    when (date) {
        today -> strToday
        tomorrow -> strTomorrow
        else -> SimpleDateFormat("EEEE", Locale.getDefault()).format(date!!)
    }
} catch (_: Exception) { dateStr }

fun formatShortDate(dateStr: String): String = try {
    SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        .parse(dateStr)?.let { SimpleDateFormat("MMM dd", Locale.getDefault()).format(it) } ?: dateStr
} catch (_: Exception) { dateStr }

fun getWeatherIcon(weatherMain: String): Int = when (weatherMain.lowercase()) {
    "clear"                -> R.drawable.ic_sunny_white
    "clouds", "cloudy"     -> R.drawable.ic_cloudy_white
    "rain", "drizzle"      -> R.drawable.ic_rainy_white
    else                   -> R.drawable.ic_sunny_white
}

fun getWeatherIconBlue(weatherMain: String): Int = when (weatherMain.lowercase()) {
    "clear"                -> R.drawable.ic_sunny_blue
    "clouds", "cloudy"     -> R.drawable.ic_cloudy_blue
    "rain", "drizzle"      -> R.drawable.ic_rainy_blue
    else                   -> R.drawable.ic_sunny_blue
}
