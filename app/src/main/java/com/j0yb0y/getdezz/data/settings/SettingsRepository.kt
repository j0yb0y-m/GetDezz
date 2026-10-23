package com.j0yb0y.getdezz.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class UserSettings(
    val maxParallelDownloads: Int = 2,
    val updateChannel: String = "STABLE",
    val defaultVideoFormat: String = "mp4",
    val defaultAudioFormat: String = "mp3",
    val embedMetadata: Boolean = true,
    val embedThumbnail: Boolean = true,
    val useAria2c: Boolean = false
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val MAX_PARALLEL_DOWNLOADS = intPreferencesKey("max_parallel_downloads")
        val UPDATE_CHANNEL = stringPreferencesKey("update_channel")
        val DEFAULT_VIDEO_FORMAT = stringPreferencesKey("default_video_format")
        val DEFAULT_AUDIO_FORMAT = stringPreferencesKey("default_audio_format")
        val EMBED_METADATA = booleanPreferencesKey("embed_metadata")
        val EMBED_THUMBNAIL = booleanPreferencesKey("embed_thumbnail")
        val USE_ARIA2C = booleanPreferencesKey("use_aria2c")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            maxParallelDownloads = preferences[Keys.MAX_PARALLEL_DOWNLOADS] ?: 2,
            updateChannel = preferences[Keys.UPDATE_CHANNEL] ?: "STABLE",
            defaultVideoFormat = preferences[Keys.DEFAULT_VIDEO_FORMAT] ?: "mp4",
            defaultAudioFormat = preferences[Keys.DEFAULT_AUDIO_FORMAT] ?: "mp3",
            embedMetadata = preferences[Keys.EMBED_METADATA] ?: true,
            embedThumbnail = preferences[Keys.EMBED_THUMBNAIL] ?: true,
            useAria2c = preferences[Keys.USE_ARIA2C] ?: false
        )
    }

    suspend fun setMaxParallelDownloads(value: Int) {
        val clamped = value.coerceIn(1, 4)
        context.dataStore.edit { preferences ->
            preferences[Keys.MAX_PARALLEL_DOWNLOADS] = clamped
        }
    }

    suspend fun setUpdateChannel(channel: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.UPDATE_CHANNEL] = channel
        }
    }

    suspend fun setDefaultVideoFormat(format: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DEFAULT_VIDEO_FORMAT] = format
        }
    }

    suspend fun setDefaultAudioFormat(format: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DEFAULT_AUDIO_FORMAT] = format
        }
    }

    suspend fun setEmbedMetadata(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.EMBED_METADATA] = enabled
        }
    }

    suspend fun setEmbedThumbnail(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.EMBED_THUMBNAIL] = enabled
        }
    }

    suspend fun setUseAria2c(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.USE_ARIA2C] = enabled
        }
    }
}
