package com.cityfix.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class ReportSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            showSyncNotification()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    private fun showSyncNotification() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Report Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications about your report status changes"
        }
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("CityFix")
            .setContentText("Your reports have been synced")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "cityfix_reports"
        private const val NOTIFICATION_ID = 1001
        private const val MAX_RETRIES = 3
        private const val WORK_NAME = "report_sync_worker"

        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<ReportSyncWorker>(
                repeatInterval = 6,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
