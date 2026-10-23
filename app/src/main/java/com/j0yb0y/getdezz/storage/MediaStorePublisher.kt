package com.j0yb0y.getdezz.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.j0yb0y.getdezz.data.MediaType
import java.io.File
import java.io.FileInputStream

object MediaStorePublisher {

    fun publishToMediaStore(
        context: Context,
        sourceFile: File,
        mediaType: MediaType,
        title: String
    ): String? {
        if (!sourceFile.exists() || sourceFile.length() == 0L) return null

        val extension = sourceFile.extension.lowercase()
        val mimeType = getMimeType(extension)
        val resolver = context.contentResolver

        val collectionUri: Uri
        val relativePath: String

        if (mediaType == MediaType.VIDEO) {
            collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            relativePath = "Movies/GetDezz"
        } else {
            collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            relativePath = "Music/GetDezz"
        }

        val fileName = sourceFile.name

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val insertedUri = resolver.insert(collectionUri, contentValues) ?: return null

        try {
            resolver.openOutputStream(insertedUri)?.use { outputStream ->
                FileInputStream(sourceFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(insertedUri, contentValues, null, null)
            }

            return insertedUri.toString()
        } catch (e: Exception) {
            resolver.delete(insertedUri, null, null)
            return null
        }
    }

    private fun getMimeType(extension: String): String {
        return when (extension) {
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "opus" -> "audio/ogg"
            "flac" -> "audio/flac"
            "wav" -> "audio/wav"
            else -> if (extension in listOf("mp4", "mkv", "webm", "avi", "mov")) "video/*" else "audio/*"
        }
    }
}
