package com.example.skymood.presentation.weatheralerts.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertSettingsDialog(
    onDismiss: () -> Unit,
    onSave: (startTimeMillis: Long, endTimeMillis: Long, alertType: String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val defaultStart = Calendar.getInstance().apply {
        add(Calendar.HOUR_OF_DAY, 1)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val defaultEnd = Calendar.getInstance().apply {
        timeInMillis = defaultStart.timeInMillis
        add(Calendar.HOUR_OF_DAY, 4)
    }

    var startTimeMillis by remember { mutableLongStateOf(defaultStart.timeInMillis) }
    var endTimeMillis by remember { mutableLongStateOf(defaultEnd.timeInMillis) }
    var alertType by remember { mutableStateOf("NOTIFICATION") }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A2A44)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alert Settings",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 20.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "DURATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "START",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.4f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showStartPicker = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF243B5E)
                        ) {
                            Text(
                                text = timeFormat.format(Date(startTimeMillis)),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 14.dp)
                            )
                        }
                    }

                    Text(
                        text = "→",
                        fontSize = 20.sp,
                        color = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "END",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.4f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showEndPicker = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF243B5E)
                        ) {
                            Text(
                                text = timeFormat.format(Date(endTimeMillis)),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "ALERT TYPE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AlertTypeOption(
                        label = "Notification",
                        subtitle = "Silent push\nalert",
                        isSelected = alertType == "NOTIFICATION",
                        onClick = { alertType = "NOTIFICATION" },
                        modifier = Modifier.weight(1f)
                    )

                    AlertTypeOption(
                        label = "Alarm",
                        subtitle = "Audio &\nvibration",
                        isSelected = alertType == "ALARM",
                        onClick = { alertType = "ALARM" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        val now = System.currentTimeMillis()
                        when {
                            startTimeMillis <= now -> {
                                Toast.makeText(context, "Start time must be in the future", Toast.LENGTH_SHORT).show()
                            }
                            endTimeMillis <= startTimeMillis -> {
                                Toast.makeText(context, "End time must be after start time", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                onSave(startTimeMillis, endTimeMillis, alertType)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E88E5)
                    )
                ) {
                    Text(
                        text = "Save Alert Settings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Discard Changes",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }

    if (showStartPicker) {
        TimePickerDialog(
            initialTimeMillis = startTimeMillis,
            onConfirm = { millis ->
                startTimeMillis = millis
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        TimePickerDialog(
            initialTimeMillis = endTimeMillis,
            onConfirm = { millis ->
                endTimeMillis = millis
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }
}

@Composable
fun AlertTypeOption(
    label: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Color(0xFF1E88E5) else Color.Transparent
    val bgColor = if (isSelected) Color(0xFF1E88E5).copy(alpha = 0.15f) else Color(0xFF243B5E)

    Surface(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF1E88E5),
                    unselectedColor = Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier.size(20.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTimeMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialTimeMillis }

    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = false
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A2A44)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SET ALERT TIME",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFF243B5E),
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = Color.White.copy(alpha = 0.7f),
                        selectorColor = Color(0xFF1E88E5),
                        containerColor = Color(0xFF1A2A44),
                        periodSelectorBorderColor = Color(0xFF1E88E5),
                        periodSelectorSelectedContainerColor = Color(0xFF1E88E5),
                        periodSelectorUnselectedContainerColor = Color.Transparent,
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = Color.White.copy(alpha = 0.6f),
                        timeSelectorSelectedContainerColor = Color(0xFF243B5E),
                        timeSelectorUnselectedContainerColor = Color(0xFF1A2A44).copy(alpha = 0.5f),
                        timeSelectorSelectedContentColor = Color(0xFF4FC3F7),
                        timeSelectorUnselectedContentColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val selectedCalendar = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE, timePickerState.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)

                                if (timeInMillis <= System.currentTimeMillis()) {
                                    add(Calendar.DAY_OF_MONTH, 1)
                                }
                            }
                            onConfirm(selectedCalendar.timeInMillis)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
