package com.example.skymood.presentation.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skymood.data.weather.model.WeatherResponse
import com.example.skymood.R

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val weatherData by viewModel.weatherData.collectAsState()

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF1D3349), Color(0xFF0C1623))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .verticalScroll(rememberScrollState())
            .padding(top = 40.dp, bottom = 16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text("San Francisco", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF4FC3F7), modifier = Modifier.size(14.dp))
                Text(" CURRENT LOCATION", fontSize = 12.sp, color = Color.Gray)
            }

            Text("24°", fontSize = 100.sp, color = Color.White, fontWeight = FontWeight.ExtraLight)
            Text("Monday, 12 Oct | 12:00 PM", color = Color.White.copy(alpha = 0.7f))

            Spacer(modifier = Modifier.height(16.dp))

            WeatherDetailsCard()

            Spacer(modifier = Modifier.height(24.dp))

            Text("Hourly Forecast", modifier = Modifier.fillMaxWidth().padding(start = 16.dp), color = Color.White, fontWeight = FontWeight.Bold)
            HourlyForecastList()

            Spacer(modifier = Modifier.height(24.dp))

            Text("5-Day Forecast", modifier = Modifier.fillMaxWidth().padding(start = 16.dp), color = Color.White, fontWeight = FontWeight.Bold)
            DailyForecastSection()
        }
    }
}

@Composable
fun WeatherDetailsCard() {
    Surface(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailItem("HUMIDITY", "65%", R.drawable.ic_humidity_blue)
                DetailItem("WIND", "12 km/h", R.drawable.ic_wind_blue)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailItem("PRESSURE", "1012 hPa", R.drawable.ic_pressure_blue)
                DetailItem("CLOUDS", "20%", R.drawable.ic_clouds_blue)
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, iconRes: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HourlyForecastList() {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(8) { index ->
            HourlyItem(isSelected = index == 0)
        }
    }
}

@Composable
fun HourlyItem(isSelected: Boolean) {
    Surface(
        color = if (isSelected) Color(0xFF1E88E5) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(70.dp).height(120.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
            Text("12 PM", color = Color.White, fontSize = 12.sp)
            Image(painter = painterResource(id = R.drawable.ic_sunny_white), contentDescription = null, modifier = Modifier.size(24.dp))
            Text("24°", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DailyForecastSection() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(5) {
            DailyItem()
        }
    }
}

@Composable
fun DailyItem() {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Tomorrow", color = Color.White)
                Text("Oct 13", color = Color.Gray, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.ic_cloudy_blue), contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cloudy", color = Color.White)
            }
            Text("22° / 18°", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}