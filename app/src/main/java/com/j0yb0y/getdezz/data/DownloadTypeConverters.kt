package com.j0yb0y.getdezz.data

import androidx.room.TypeConverter

class DownloadTypeConverters {
    @TypeConverter
    fun fromDownloadStatus(status: DownloadStatus): String = status.name

    @TypeConverter
    fun toDownloadStatus(value: String): DownloadStatus =
        try { DownloadStatus.valueOf(value) } catch (e: Exception) { DownloadStatus.FAILED }

    @TypeConverter
    fun fromMediaType(mediaType: MediaType): String = mediaType.name

    @TypeConverter
    fun toMediaType(value: String): MediaType =
        try { MediaType.valueOf(value) } catch (e: Exception) { MediaType.VIDEO }
}
