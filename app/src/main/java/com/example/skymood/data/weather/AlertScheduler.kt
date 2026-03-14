package com.example.skymood.data.weather

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.skymood.data.database.AlertEntity
import java.util.concurrent.TimeUnit

object AlertScheduler {

    fun scheduleAlert(context: Context, alert: AlertEntity) {
        val now = System.currentTimeMillis()
        val delay = alert.startTimeMillis - now

        if (delay <= 0) return 

        val inputData = Data.Builder()
            .putInt("alert_id", alert.id)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<AlertWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("alert_${alert.id}")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "alert_${alert.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun scheduleNextHourlyAlert(context: Context, alertId: Int, nextFireTimeMillis: Long) {
        val now = System.currentTimeMillis()
        val delay = nextFireTimeMillis - now

        if (delay <= 0) return

        val inputData = Data.Builder()
            .putInt("alert_id", alertId)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<AlertWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("alert_$alertId")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "alert_$alertId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelAlert(context: Context, alertId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork("alert_$alertId")
    }
}
