package com.j0yb0y.getdezz.engine

import android.content.Context
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDL.UpdateChannel
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

sealed class EngineState {
    object Initializing : EngineState()
    object Ready : EngineState()
    data class Failed(val error: String) : EngineState()
}

data class StreamFormat(
    val formatId: String,
    val ext: String,
    val resolution: String?,
    val note: String?,
    val filesize: Long,
    val isVideo: Boolean,
    val isAudio: Boolean,
    val vcodec: String?,
    val acodec: String?
)

data class StreamInfo(
    val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val duration: Long,
    val uploader: String?,
    val url: String,
    val formats: List<StreamFormat>
)

class EngineRepository(private val context: Context) {
    private val _engineState = MutableStateFlow<EngineState>(EngineState.Initializing)
    val engineState: StateFlow<EngineState> = _engineState.asStateFlow()

    suspend fun initEngine() = withContext(Dispatchers.IO) {
        if (_engineState.value is EngineState.Ready) return@withContext
        try {
            _engineState.value = EngineState.Initializing
            YoutubeDL.getInstance().init(context)
            FFmpeg.getInstance().init(context)
            _engineState.value = EngineState.Ready
        } catch (e: Exception) {
            _engineState.value = EngineState.Failed(e.localizedMessage ?: "Engine initialization failed")
        }
    }

    suspend fun awaitReady() {
        engineState.first { it is EngineState.Ready || it is EngineState.Failed }
        if (engineState.value is EngineState.Failed) {
            val error = (engineState.value as EngineState.Failed).error
            throw IllegalStateException("Engine is not ready: $error")
        }
    }

    fun getJsRuntimeOption(): String? {
        val nativeDir = context.applicationInfo.nativeLibraryDir
        val denoFile = File(nativeDir, "libdeno.so")
        val qjsFile = File(nativeDir, "libqjs.so")
        return when {
            denoFile.exists() -> "deno:${denoFile.absolutePath}"
            qjsFile.exists() -> "quickjs:${qjsFile.absolutePath}"
            else -> null
        }
    }

    suspend fun fetchStreamInfo(url: String): Result<StreamInfo> = withContext(Dispatchers.IO) {
        try {
            awaitReady()
            val info: VideoInfo = YoutubeDL.getInstance().getInfo(url)
            val formats = info.formats?.map { f ->
                val size = if (f.fileSize > 0) f.fileSize else f.fileSizeApproximate
                StreamFormat(
                    formatId = f.formatId ?: "",
                    ext = f.ext ?: "",
                    resolution = f.formatNote,
                    note = f.formatNote,
                    filesize = size,
                    isVideo = f.vcodec != null && f.vcodec != "none",
                    isAudio = f.acodec != null && f.acodec != "none",
                    vcodec = f.vcodec,
                    acodec = f.acodec
                )
            } ?: emptyList()

            val streamInfo = StreamInfo(
                id = info.id ?: "",
                title = info.title ?: "",
                thumbnailUrl = info.thumbnail,
                duration = info.duration.toLong(),
                uploader = info.uploader,
                url = url,
                formats = formats
            )
            Result.success(streamInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateYtDlp(channelName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            awaitReady()
            val channel = if (channelName.equals("NIGHTLY", ignoreCase = true)) {
                UpdateChannel.NIGHTLY
            } else {
                UpdateChannel.STABLE
            }
            val status = YoutubeDL.getInstance().updateYoutubeDL(context, channel)
            Result.success(status?.name ?: "UPDATE_SUCCESS")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
