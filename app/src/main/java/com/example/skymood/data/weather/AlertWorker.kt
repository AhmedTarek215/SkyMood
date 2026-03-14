package com.example.skymood.data.weather

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.skymood.R
import com.example.skymood.data.database.WeatherDatabase
import com.example.skymood.data.weather.datasource.local.WeatherLocalDataSource
import com.example.skymood.data.weather.datasource.remote.WeatherRemoteDataSource
import com.example.skymood.presentation.weatheralerts.AlertOverlayActivity
import com.example.skymood.utils.Constants

class AlertWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "weather_alerts_channel"
        const val ALARM_CHANNEL_ID = "weather_alarm_channel"
        private const val ONE_HOUR_MILLIS = 60 * 60 * 1000L
    }

    override suspend fun doWork(): Result {
        val alertId = inputData.getInt("alert_id", -1)
        if (alertId == -1) return Result.success()

        val db = WeatherDatabase.getDatabase(context)
        val dao = db.weatherDao()
        val alert = dao.getAlertById(alertId) ?: return Result.success()

        if (!alert.isEnabled) return Result.success()

        val now = System.currentTimeMillis()

        if (now >= alert.endTimeMillis) {
            dao.deleteAlertById(alertId)
            return Result.success()
        }

        val remoteDataSource = WeatherRemoteDataSource()
        var weatherDescription = "Weather alert active"
        var temperature = ""
        try {
            val weatherData = remoteDataSource.getForecast(
                alert.lat, alert.lon,
                Constants.WEATHER_API_KEY,
                "metric", "en"
            )
            if (weatherData != null) {
                val current = weatherData.list.firstOrNull()
                val desc = current?.weather?.firstOrNull()?.description ?: "N/A"
                val temp = current?.main?.temp?.toInt() ?: 0
                weatherDescription = desc.replaceFirstChar { it.uppercase() }
                temperature = "${temp}°C"
            }
        } catch (_: Exception) { }

        when (alert.alertType) {
            "NOTIFICATION" -> showNotification(alert.cityName, weatherDescription, temperature, alertId)
            "ALARM" -> showAlarmOverlay(alert.cityName, weatherDescription, temperature, alertId)
        }

        val nextFireTime = now + ONE_HOUR_MILLIS
        if (nextFireTime <= alert.endTimeMillis) {
            AlertScheduler.scheduleNextHourlyAlert(context, alertId, nextFireTime)
        } else {
            AlertScheduler.scheduleNextHourlyAlert(context, alertId, alert.endTimeMillis)
        }

        return Result.success()
    }

    private fun showNotification(cityName: String, description: String, temp: String, alertId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Weather Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            this.description = "Weather alert notifications"
            enableVibration(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)

        val fullScreenIntent = Intent(context, AlertOverlayActivity::class.java).apply {
            putExtra("alert_id", alertId)
            putExtra("city_name", cityName)
            putExtra("weather_desc", description)
            putExtra("temperature", temp)
            putExtra("alert_type", "NOTIFICATION")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, alertId, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss action
        val dismissIntent = Intent(context, AlertDismissReceiver::class.java).apply {
            putExtra("alert_id", alertId)
            putExtra("notification_id", alertId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context, alertId, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bell_blue)
            .setContentTitle("⛅ Weather Update for $cityName")
            .setContentText("It's currently $temp with $description.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                .bigText("Hi there! The current weather in $cityName is $temp with $description.\nStay safe and have a great day!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_bell_grey, "Dismiss", dismissPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(alertId, notification)
    }

    private fun showAlarmOverlay(cityName: String, description: String, temp: String, alertId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val channel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "Weather Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            this.description = "Weather alarm notifications"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
            setSound(alarmSound, AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)

        val overlayIntent = Intent(context, AlertOverlayActivity::class.java).apply {
            putExtra("alert_id", alertId)
            putExtra("city_name", cityName)
            putExtra("weather_desc", description)
            putExtra("temperature", temp)
            putExtra("alert_type", "ALARM")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, alertId + 1000, overlayIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss action
        val dismissIntent = Intent(context, AlertDismissReceiver::class.java).apply {
            putExtra("alert_id", alertId)
            putExtra("notification_id", alertId + 1000)
            putExtra("stop_alarm", true)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context, alertId + 1000, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bell_blue)
            .setContentTitle("🚨 Weather Alarm: $cityName")
            .setContentText("It is $temp and $description.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                .bigText("Wake up! The weather in $cityName is currently $temp with $description.\nTap to dismiss the alarm.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_bell_grey, "Dismiss", dismissPendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()

        notificationManager.notify(alertId + 1000, notification)

        context.startActivity(overlayIntent)
    }
}
