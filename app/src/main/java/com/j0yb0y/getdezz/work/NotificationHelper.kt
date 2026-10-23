package com.j0yb0y.getdezz.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.WorkManager
import com.j0yb0y.getdezz.R
import java.util.UUID

object NotificationHelper {
    const val CHANNEL_ID = "getdezz_downloads"
    private const val CHANNEL_NAME = "Downloads"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "GetDezz background download progress"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun buildDownloadNotification(
        context: Context,
        jobId: String,
        title: String,
        progress: Float,
        speed: String?,
        eta: String?,
        isProcessing: Boolean = false
    ): Notification {
        createNotificationChannel(context)

        val contentText = when {
            isProcessing -> context.getString(R.string.download_processing)
            progress < 0f -> context.getString(R.string.download_downloading)
            else -> {
                val parts = mutableListOf<String>()
                parts.add("${progress.toInt()}%")
                if (!speed.isNullOrEmpty()) parts.add(speed)
                if (!eta.isNullOrEmpty()) parts.add(eta)
                parts.joinToString(" • ")
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        if (isProcessing || progress < 0f) {
            builder.setProgress(100, 0, true)
        } else {
            builder.setProgress(100, progress.toInt().coerceIn(0, 100), false)
        }

        val workUuid = try { UUID.fromString(jobId) } catch (_: Exception) { null }
        if (workUuid != null) {
            val cancelPendingIntent = WorkManager.getInstance(context).createCancelPendingIntent(workUuid)
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.btn_cancel), cancelPendingIntent)
        }

        return builder.build()
    }
}
