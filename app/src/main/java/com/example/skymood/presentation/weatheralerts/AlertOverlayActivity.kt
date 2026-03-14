package com.example.skymood.presentation.weatheralerts

import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skymood.ui.theme.SkyMoodTheme

class AlertOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val alertId = intent.getIntExtra("alert_id", -1)
        val cityName = intent.getStringExtra("city_name") ?: "Unknown"
        val weatherDesc = intent.getStringExtra("weather_desc") ?: "Weather Alert"
        val temperature = intent.getStringExtra("temperature") ?: ""
        val alertType = intent.getStringExtra("alert_type") ?: "NOTIFICATION"

        setContent {
            SkyMoodTheme {
                AlertOverlayScreen(
                    cityName = cityName,
                    weatherDesc = weatherDesc,
                    temperature = temperature,
                    alertType = alertType,
                    onDismiss = {
                        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        nm.cancel(alertId)
                        nm.cancel(alertId + 1000)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun AlertOverlayScreen(
    cityName: String,
    weatherDesc: String,
    temperature: String,
    alertType: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A1628),
                        Color(0xFF0D1F3C),
                        Color(0xFF0A1628)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A2A44)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = if (alertType == "ALARM") Color(0xFFFF6B35).copy(alpha = 0.2f)
                            else Color(0xFF4FC3F7).copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (alertType == "ALARM") "🚨" else "⛅",
                        fontSize = 36.sp
                    )
                }

                Text(
                    text = if (alertType == "ALARM") "Weather Alarm" else "Weather Alert",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = cityName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4FC3F7)
                )

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                if (temperature.isNotEmpty()) {
                    Text(
                        text = temperature,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = weatherDesc,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (alertType == "ALARM") Color(0xFFFF6B35)
                        else Color(0xFF1E88E5)
                    )
                ) {
                    Text(
                        text = "Dismiss",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
