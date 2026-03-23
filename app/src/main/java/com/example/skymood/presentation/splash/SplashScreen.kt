package com.example.skymood.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skymood.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {

    // ── Animation states ──────────────────────────────────────
    var startAnimation by remember { mutableStateOf(false) }

    // Icon: scale up + fade in
    val iconScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutBack),
        label = "iconScale"
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "iconAlpha"
    )

    // Title: fade in with slight delay
    val titleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 400, easing = FastOutSlowInEasing),
        label = "titleAlpha"
    )

    // Tagline: fade in with more delay
    val taglineAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 700, easing = FastOutSlowInEasing),
        label = "taglineAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500L)
        onSplashFinished()
    }

    // ── UI ─────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B1628),  // deep navy at top
                        Color(0xFF0A1A2E),  // dark blue-black middle
                        Color(0xFF081222)   // near-black bottom
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── App Icon ───────────────────────────────────────
            Image(
                painter = painterResource(id = R.drawable.ic_splash),
                contentDescription = "SkyMood Logo",
                modifier = Modifier
                    .size(180.dp)
                    .scale(iconScale)
                    .alpha(iconAlpha)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── App Name ───────────────────────────────────────
            Text(
                text = "SkyMood",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Tagline ────────────────────────────────────────
            Text(
                text = "F E E L   T H E   F O R E C A S T",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4A8CC7),     // muted sky-blue
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha)
            )
        }
    }
}
