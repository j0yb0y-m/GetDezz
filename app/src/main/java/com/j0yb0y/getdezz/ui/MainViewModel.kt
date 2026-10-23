package com.j0yb0y.getdezz.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.j0yb0y.getdezz.GetDezzApp
import com.j0yb0y.getdezz.data.DownloadEntity
import com.j0yb0y.getdezz.data.DownloadStatus
import com.j0yb0y.getdezz.data.MediaType
import com.j0yb0y.getdezz.data.repository.DownloadRepository
import com.j0yb0y.getdezz.data.settings.SettingsRepository
import com.j0yb0y.getdezz.engine.EngineRepository
import com.j0yb0y.getdezz.engine.EngineState
import com.j0yb0y.getdezz.engine.StreamInfo
import com.j0yb0y.getdezz.work.DownloadScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(
    private val downloadRepository: DownloadRepository,
    private val settingsRepository: SettingsRepository,
    private val engineRepository: EngineRepository
) : ViewModel() {

    val allDownloads = downloadRepository.allDownloads
    val userSettings = settingsRepository.settingsFlow
    val engineState: StateFlow<EngineState> = engineRepository.engineState

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _isFetching = MutableStateFlow(false)
    val isFetching: StateFlow<Boolean> = _isFetching.asStateFlow()

    private val _streamInfo = MutableStateFlow<StreamInfo?>(null)
    val streamInfo: StateFlow<StreamInfo?> = _streamInfo.asStateFlow()

    private val _fetchError = MutableStateFlow<String?>(null)
    val fetchError: StateFlow<String?> = _fetchError.asStateFlow()

    private val _updateStatus = MutableStateFlow<String?>(null)
    val updateStatus: StateFlow<String?> = _updateStatus.asStateFlow()

    fun onUrlInputChanged(url: String) {
        _urlInput.value = url
    }

    fun clearStreamInfo() {
        _streamInfo.value = null
        _fetchError.value = null
    }

    fun fetchStreamInfo(url: String = _urlInput.value) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            _isFetching.value = true
            _fetchError.value = null
            _streamInfo.value = null

            val result = engineRepository.fetchStreamInfo(trimmed)
            _isFetching.value = false

            result.onSuccess { info ->
                _streamInfo.value = info
            }.onFailure { err ->
                _fetchError.value = err.localizedMessage ?: "Failed to fetch stream info"
            }
        }
    }

    fun startDownload(
        context: Context,
        url: String,
        title: String,
        thumbnailUrl: String?,
        duration: Long,
        uploader: String?,
        mediaType: MediaType,
        format: String,
        quality: String
    ) {
        val downloadId = UUID.randomUUID().toString()
        val entity = DownloadEntity(
            id = downloadId,
            url = url,
            title = title.ifBlank { "Download" },
            thumbnailUrl = thumbnailUrl,
            duration = duration,
            uploader = uploader,
            format = format,
            quality = quality,
            mediaType = mediaType,
            status = DownloadStatus.QUEUED
        )

        viewModelScope.launch {
            downloadRepository.insertDownload(entity)
            DownloadScheduler.scheduleDownload(context, downloadId)
            clearStreamInfo()
            _urlInput.value = ""
        }
    }

    fun cancelDownload(context: Context, downloadId: String) {
        viewModelScope.launch {
            DownloadScheduler.cancelDownload(context, downloadId)
            downloadRepository.updateStatus(downloadId, DownloadStatus.CANCELLED)
        }
    }

    fun deleteDownload(downloadId: String) {
        viewModelScope.launch {
            downloadRepository.deleteDownload(downloadId)
        }
    }

    fun clearFinishedDownloads() {
        viewModelScope.launch {
            downloadRepository.clearFinishedDownloads()
        }
    }

    fun updateYtDlp(channel: String) {
        viewModelScope.launch {
            _updateStatus.value = "Updating yt-dlp..."
            val res = engineRepository.updateYtDlp(channel)
            res.onSuccess { msg ->
                _updateStatus.value = "Update finished: $msg"
            }.onFailure { err ->
                _updateStatus.value = "Update error: ${err.localizedMessage}"
            }
        }
    }

    fun updateMaxParallelDownloads(value: Int) {
        viewModelScope.launch {
            settingsRepository.setMaxParallelDownloads(value)
        }
    }

    fun updateUpdateChannel(channel: String) {
        viewModelScope.launch {
            settingsRepository.setUpdateChannel(channel)
        }
    }

    fun updateDefaultVideoFormat(format: String) {
        viewModelScope.launch {
            settingsRepository.setDefaultVideoFormat(format)
        }
    }

    fun updateDefaultAudioFormat(format: String) {
        viewModelScope.launch {
            settingsRepository.setDefaultAudioFormat(format)
        }
    }

    fun updateEmbedMetadata(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setEmbedMetadata(enabled)
        }
    }

    fun updateEmbedThumbnail(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setEmbedThumbnail(enabled)
        }
    }

    fun updateUseAria2c(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseAria2c(enabled)
        }
    }

    class Factory(private val app: GetDezzApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(
                app.container.downloadRepository,
                app.container.settingsRepository,
                app.container.engineRepository
            ) as T
        }
    }
}
