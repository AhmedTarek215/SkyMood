import re

home_path = "app/src/main/java/com/example/skymood/presentation/home/HomeScreen.kt"
components_path = "app/src/main/java/com/example/skymood/presentation/home/WeatherComponents.kt"

with open(home_path, "r", encoding="utf-8") as f:
    lines = f.readlines()

bg_gradient_start = -1
bg_gradient_end = -1
for i, line in enumerate(lines):
    if line.startswith("private fun getBackgroundGradient"):
        bg_gradient_start = i
    if bg_gradient_start != -1 and line.startswith("}") and i > bg_gradient_start:
        bg_gradient_end = i
        break

content_view_start = -1
for i, line in enumerate(lines):
    if line.startswith("@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)"):
        content_view_start = i
        break

header = """package com.example.skymood.presentation.home

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skymood.R
import com.example.skymood.data.weather.model.WeatherResponse
import java.text.SimpleDateFormat
import java.util.*

"""

bg_gradient_code = "".join(lines[bg_gradient_start:bg_gradient_end+1]).replace("private fun getBackgroundGradient", "fun getBackgroundGradient")
components_code = "".join(lines[content_view_start:])
components_code = components_code.replace("private fun WeatherContentView", "fun WeatherContentView")
components_code = components_code.replace(
    "isOffline: Boolean\n)", 
    "isOffline: Boolean,\n    showCurrentLocationLabel: Boolean = true\n)"
)
components_code = components_code.replace(
    """            Row(
                verticalAlignment = Alignment.CenterVertically,""",
    """            if (showCurrentLocationLabel) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,"""
)
components_code = components_code.replace(
    """                Text(" CURRENT LOCATION", fontSize = 10.sp, color = Color(0xFF4FC3F7), letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold)
            }""",
    """                    Text(" CURRENT LOCATION", fontSize = 10.sp, color = Color(0xFF4FC3F7), letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold)
                }
            }"""
)

with open(components_path, "w", encoding="utf-8") as f:
    f.write(header + bg_gradient_code + "\n\n" + components_code)

new_home_lines = lines[:bg_gradient_start] + lines[bg_gradient_end+1:content_view_start]
with open(home_path, "w", encoding="utf-8") as f:
    f.writelines(new_home_lines)

print("Extraction completed successfully.")
