package com.j0yb0y.getdezz.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    fun getDownloadById(id: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadByIdOnce(id: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE status IN ('QUEUED', 'DOWNLOADING', 'PROCESSING') ORDER BY createdAt ASC")
    suspend fun getActiveDownloads(): List<DownloadEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Query("UPDATE downloads SET status = :status, progress = :progress, speed = :speed, eta = :eta, downloadedBytes = :downloadedBytes, totalBytes = :totalBytes WHERE id = :id")
    suspend fun updateProgress(
        id: String,
        status: DownloadStatus,
        progress: Float,
        speed: String?,
        eta: String?,
        downloadedBytes: Long,
        totalBytes: Long
    )

    @Query("UPDATE downloads SET status = :status, errorMessage = :errorMessage, filePath = :filePath, completedAt = :completedAt WHERE id = :id")
    suspend fun updateStatus(
        id: String,
        status: DownloadStatus,
        errorMessage: String? = null,
        filePath: String? = null,
        completedAt: Long? = if (status == DownloadStatus.COMPLETED) System.currentTimeMillis() else null
    )

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownload(id: String)

    @Query("DELETE FROM downloads WHERE status IN ('COMPLETED', 'FAILED', 'CANCELLED')")
    suspend fun clearFinishedDownloads()
}
