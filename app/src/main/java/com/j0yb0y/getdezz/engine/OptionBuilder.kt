package com.j0yb0y.getdezz.engine

import com.j0yb0y.getdezz.data.MediaType
import com.yausername.youtubedl_android.YoutubeDLRequest
import java.io.File

object OptionBuilder {

    fun buildRequest(
        url: String,
        tmpDir: File,
        mediaType: MediaType,
        format: String,
        quality: String,
        embedMetadata: Boolean = true,
        embedThumbnail: Boolean = true,
        jsRuntimeOption: String? = null,
        useAria2c: Boolean = false,
        isPlaylist: Boolean = false
    ): YoutubeDLRequest {
        val request = YoutubeDLRequest(url)

        val outputTemplate = File(tmpDir, "%(id)s.%(ext)s").absolutePath
        request.addOption("-o", outputTemplate)

        if (!isPlaylist) {
            request.addOption("--no-playlist")
        }

        if (mediaType == MediaType.VIDEO) {
            val container = format.lowercase()
            val height = quality.replace("p", "").toIntOrNull() ?: 1080
            val audioExt = if (container == "mp4") "m4a" else "opus"

            request.addOption("-f", "bv*+ba/b")
            request.addOption("-S", "res:$height,ext:$container:$audioExt")
            request.addOption("--merge-output-format", container)
        } else {
            val audioFmt = format.lowercase()
            request.addOption("-x")
            request.addOption("--audio-format", audioFmt)
        }

        if (embedMetadata) {
            request.addOption("--embed-metadata")
        }
        if (embedThumbnail) {
            request.addOption("--embed-thumbnail")
        }

        if (!jsRuntimeOption.isNullOrEmpty()) {
            request.addOption("--js-runtimes", jsRuntimeOption)
        }

        if (useAria2c) {
            request.addOption("--downloader", "aria2c")
        }

        return request
    }
}
