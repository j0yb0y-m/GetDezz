package com.j0yb0y.getdezz.work

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object DownloadScheduler {
    fun scheduleDownload(context: Context, downloadId: String) {
        val inputData = Data.Builder()
            .putString(DownloadWorker.KEY_DOWNLOAD_ID, downloadId)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "download_$downloadId",
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancelDownload(context: Context, downloadId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("download_$downloadId")
    }
}
