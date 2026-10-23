package com.j0yb0y.getdezz.work

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.j0yb0y.getdezz.GetDezzApp
import com.j0yb0y.getdezz.data.DownloadStatus
import com.j0yb0y.getdezz.engine.OptionBuilder
import com.j0yb0y.getdezz.storage.MediaStorePublisher
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class DownloadWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_DOWNLOAD_ID = "download_id"
        private val globalSemaphoreMap = mutableMapOf<Int, Semaphore>()

        @Synchronized
        private fun getSemaphore(maxParallel: Int): Semaphore {
            return globalSemaphoreMap.getOrPut(maxParallel) { Semaphore(maxParallel) }
        }
    }

    override suspend fun doWork(): androidx.work.ListenableWorker.Result = withContext(Dispatchers.IO) {
        val downloadId = inputData.getString(KEY_DOWNLOAD_ID) ?: return@withContext androidx.work.ListenableWorker.Result.failure()
        val app = appContext.applicationContext as GetDezzApp
        val downloadRepo = app.container.downloadRepository
        val settingsRepo = app.container.settingsRepository
        val engineRepo = app.container.engineRepository

        val download = downloadRepo.getDownloadByIdOnce(downloadId) ?: return@withContext androidx.work.ListenableWorker.Result.failure()
        val settings = settingsRepo.settingsFlow.first()
        val semaphore = getSemaphore(settings.maxParallelDownloads)

        val notificationId = downloadId.hashCode()
        setForeground(createForegroundInfo(notificationId, download.title, 0f, null, null, false))

        val tmpDir = File(appContext.getExternalFilesDir("tmp"), downloadId)
        if (!tmpDir.exists()) tmpDir.mkdirs()

        semaphore.acquire()
        try {
            engineRepo.awaitReady()
            downloadRepo.updateStatus(downloadId, DownloadStatus.DOWNLOADING)

            val jsRuntime = engineRepo.getJsRuntimeOption()
            var isProcessing = false
            var lastDbUpdate = 0L
            var lastNotifUpdate = 0L

            val progressCallback = { progress: Float, eta: Long, line: String ->
                val now = System.currentTimeMillis()
                if (line.contains("[Merger]") || line.contains("[ExtractAudio]") ||
                    line.contains("[VideoConvertor]") || line.contains("[FixupM4a]")
                ) {
                    isProcessing = true
                }

                val etaString = if (eta > 0) formatEta(eta) else null
                val currentStatus = if (isProcessing) DownloadStatus.PROCESSING else DownloadStatus.DOWNLOADING
                val currentProgress = if (isProcessing) -1f else progress

                if (now - lastDbUpdate >= 500) {
                    lastDbUpdate = now
                    kotlinx.coroutines.runBlocking {
                        downloadRepo.updateProgress(
                            id = downloadId,
                            status = currentStatus,
                            progress = currentProgress,
                            speed = null,
                            eta = etaString,
                            downloadedBytes = 0L,
                            totalBytes = 0L
                        )
                    }
                }

                if (now - lastNotifUpdate >= 1000) {
                    lastNotifUpdate = now
                    kotlinx.coroutines.runBlocking {
                        setForeground(
                            createForegroundInfo(
                                notificationId,
                                download.title,
                                currentProgress,
                                null,
                                etaString,
                                isProcessing
                            )
                        )
                    }
                }
            }

            val request = OptionBuilder.buildRequest(
                url = download.url,
                tmpDir = tmpDir,
                mediaType = download.mediaType,
                format = download.format,
                quality = download.quality,
                embedMetadata = settings.embedMetadata,
                embedThumbnail = settings.embedThumbnail,
                jsRuntimeOption = jsRuntime,
                useAria2c = settings.useAria2c
            )

            var success = false
            try {
                YoutubeDL.getInstance().execute(request, downloadId, progressCallback)
                success = true
            } catch (e: Exception) {
                if (isStopped) throw e
                if (settings.embedMetadata || settings.embedThumbnail) {
                    val retryRequest = OptionBuilder.buildRequest(
                        url = download.url,
                        tmpDir = tmpDir,
                        mediaType = download.mediaType,
                        format = download.format,
                        quality = download.quality,
                        embedMetadata = false,
                        embedThumbnail = false,
                        jsRuntimeOption = jsRuntime,
                        useAria2c = settings.useAria2c
                    )
                    YoutubeDL.getInstance().execute(retryRequest, downloadId, progressCallback)
                    success = true
                } else {
                    throw e
                }
            }

            if (success) {
                val output = getOutputResultFile(tmpDir)
                if (output != null && output.exists() && output.length() > 0) {
                    val mediaStoreUri = MediaStorePublisher.publishToMediaStore(
                        context = appContext,
                        sourceFile = output,
                        mediaType = download.mediaType,
                        title = download.title
                    )
                    downloadRepo.updateStatus(
                        id = downloadId,
                        status = DownloadStatus.COMPLETED,
                        filePath = mediaStoreUri ?: output.absolutePath
                    )
                } else {
                    downloadRepo.updateStatus(downloadId, DownloadStatus.FAILED, errorMessage = "Result file not found")
                }
            }

            androidx.work.ListenableWorker.Result.success()
        } catch (e: Exception) {
            val status = if (isStopped) DownloadStatus.CANCELLED else DownloadStatus.FAILED
            downloadRepo.updateStatus(downloadId, status, errorMessage = e.localizedMessage)
            androidx.work.ListenableWorker.Result.failure()
        } finally {
            semaphore.release()
            withContext(NonCancellable) {
                if (isStopped) {
                    try {
                        YoutubeDL.getInstance().destroyProcessById(downloadId)
                    } catch (_: Exception) {}
                }
                tmpDir.deleteRecursively()
            }
        }
    }

    private fun createForegroundInfo(
        notificationId: Int,
        title: String,
        progress: Float,
        speed: String?,
        eta: String?,
        isProcessing: Boolean
    ): ForegroundInfo {
        val notification = NotificationHelper.buildDownloadNotification(
            appContext,
            jobId = inputData.getString(KEY_DOWNLOAD_ID) ?: "",
            title = title,
            progress = progress,
            speed = speed,
            eta = eta,
            isProcessing = isProcessing
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    private fun getOutputResultFile(tmpDir: File): File? {
        val ignoreExtensions = setOf("part", "ytdl", "json", "temp", "jpg", "jpeg", "png", "webp")
        return tmpDir.listFiles()
            ?.filter { file -> file.isFile && file.extension.lowercase() !in ignoreExtensions }
            ?.maxByOrNull { file -> file.length() }
    }

    private fun formatEta(etaSeconds: Long): String {
        val minutes = etaSeconds / 60
        val seconds = etaSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
