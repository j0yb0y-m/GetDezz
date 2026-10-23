package com.j0yb0y.getdezz.data.repository

import com.j0yb0y.getdezz.data.DownloadDao
import com.j0yb0y.getdezz.data.DownloadEntity
import com.j0yb0y.getdezz.data.DownloadStatus
import kotlinx.coroutines.flow.Flow

class DownloadRepository(private val downloadDao: DownloadDao) {
    val allDownloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()

    fun getDownloadById(id: String): Flow<DownloadEntity?> = downloadDao.getDownloadById(id)

    suspend fun getDownloadByIdOnce(id: String): DownloadEntity? = downloadDao.getDownloadByIdOnce(id)

    suspend fun insertDownload(download: DownloadEntity) {
        downloadDao.insertDownload(download)
    }

    suspend fun updateDownload(download: DownloadEntity) {
        downloadDao.updateDownload(download)
    }

    suspend fun updateProgress(
        id: String,
        status: DownloadStatus,
        progress: Float,
        speed: String?,
        eta: String?,
        downloadedBytes: Long,
        totalBytes: Long
    ) {
        downloadDao.updateProgress(id, status, progress, speed, eta, downloadedBytes, totalBytes)
    }

    suspend fun updateStatus(
        id: String,
        status: DownloadStatus,
        errorMessage: String? = null,
        filePath: String? = null
    ) {
        downloadDao.updateStatus(id, status, errorMessage, filePath)
    }

    suspend fun deleteDownload(id: String) {
        downloadDao.deleteDownload(id)
    }

    suspend fun clearFinishedDownloads() {
        downloadDao.clearFinishedDownloads()
    }
}
