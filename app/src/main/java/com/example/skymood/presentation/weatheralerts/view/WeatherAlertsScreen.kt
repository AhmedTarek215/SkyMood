package com.example.skymood.presentation.weatheralerts.view

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skymood.R
import com.example.skymood.data.database.AlertEntity
import com.example.skymood.presentation.weatheralerts.viewmodel.WeatherAlertsViewModel
import com.google.accompanist.permissions.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WeatherAlertsScreen(
    viewModel: WeatherAlertsViewModel
) {
    val alerts by viewModel.alerts.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }

    val permissions = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
    }
    
    val permissionState = rememberMultiplePermissionsState(permissions)

    LaunchedEffect(alerts) {
        viewModel.cleanupExpiredAlerts()
    }

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
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.alerts_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = stringResource(R.string.alerts_subtitle),
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (alerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🔔",
                            fontSize = 48.sp
                        )
                        Text(
                            text = stringResource(R.string.alerts_no_alerts_yet),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = stringResource(R.string.alerts_tap_to_create),
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
                ) {
                    items(alerts, key = { it.id }) { alert ->
                        AlertCard(
                            alert = alert,
                            onToggle = { viewModel.toggleAlert(alert.id, it) },
                            onDelete = { viewModel.deleteAlert(alert) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    android.provider.Settings.canDrawOverlays(context)
                } else true
                
                if (!permissionState.allPermissionsGranted) {
                    permissionState.launchMultiplePermissionRequest()
                } else if (!hasOverlay) {
                    val intent = android.content.Intent(
                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        android.net.Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                    android.widget.Toast.makeText(context, context.getString(R.string.alerts_display_over_apps_warning), android.widget.Toast.LENGTH_LONG).show()
                } else if (currentLocation != null) {
                    showAddDialog = true
                } else {
                    android.widget.Toast.makeText(context, context.getString(R.string.alerts_waiting_location), android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp),
            containerColor = Color(0xFF1E88E5), // Blue from mockup
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Alert")
        }

        if (showAddDialog && currentLocation != null) {
            AlertSettingsDialog(
                onDismiss = { showAddDialog = false },
                onSave = { startTime, endTime, alertType ->
                    val loc = currentLocation!!
                    viewModel.addAlert(
                        lat = loc.first,
                        lon = loc.second,
                        cityName = loc.third,
                        alertType = alertType,
                        startTimeMillis = startTime,
                        endTimeMillis = endTime
                    )
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AlertCard(
    alert: AlertEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val isAlarm = alert.alertType == "ALARM"
    val badgeColor by animateColorAsState(
        targetValue = if (isAlarm) Color(0xFFFF9800) else Color(0xFF4FC3F7),
        label = "badge_color"
    )

    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp), // Rounded corners matching UI
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A2A44)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.cityName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = if (isAlarm) stringResource(R.string.alerts_alarm) else stringResource(R.string.alerts_notification),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = alert.isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF1E88E5),
                            uncheckedThumbColor = Color(0xFF707A8A),
                            uncheckedTrackColor = Color(0xFF2A3A54)
                        )
                    )
                    
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = Color.White.copy(alpha = 0.6f))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color(0xFF243B5E))
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.alerts_delete_alert), color = Color(0xFFEF5350)) },
                                onClick = { 
                                    onDelete()
                                    showMenu = false 
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2A3A54),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.alerts_start).uppercase(Locale.getDefault()),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.4f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = timeFormat.format(Date(alert.startTimeMillis)),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                Text(
                    text = "→",
                    fontSize = 20.sp,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2A3A54),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.alerts_end).uppercase(Locale.getDefault()),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.4f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = timeFormat.format(Date(alert.endTimeMillis)),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
