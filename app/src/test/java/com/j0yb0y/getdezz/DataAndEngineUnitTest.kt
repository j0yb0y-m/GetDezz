package com.j0yb0y.getdezz

import com.j0yb0y.getdezz.data.DownloadEntity
import com.j0yb0y.getdezz.data.DownloadStatus
import com.j0yb0y.getdezz.data.DownloadTypeConverters
import com.j0yb0y.getdezz.data.MediaType
import com.j0yb0y.getdezz.data.settings.UserSettings
import com.j0yb0y.getdezz.engine.StreamFormat
import com.j0yb0y.getdezz.engine.StreamInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataAndEngineUnitTest {

    @Test
    fun downloadEntity_defaultValues() {
        val entity = DownloadEntity(
            id = "test-123",
            url = "https://example.com/watch?v=123",
            title = "Test Video",
            format = "mp4",
            quality = "1080p",
            mediaType = MediaType.VIDEO
        )

        assertEquals("test-123", entity.id)
        assertEquals(DownloadStatus.QUEUED, entity.status)
        assertEquals(0f, entity.progress, 0.001f)
        assertEquals(MediaType.VIDEO, entity.mediaType)
    }

    @Test
    fun downloadTypeConverters_conversionCorrect() {
        val converters = DownloadTypeConverters()

        val statusStr = converters.fromDownloadStatus(DownloadStatus.DOWNLOADING)
        assertEquals("DOWNLOADING", statusStr)
        assertEquals(DownloadStatus.DOWNLOADING, converters.toDownloadStatus(statusStr))

        val mediaTypeStr = converters.fromMediaType(MediaType.AUDIO)
        assertEquals("AUDIO", mediaTypeStr)
        assertEquals(MediaType.AUDIO, converters.toMediaType(mediaTypeStr))

        assertEquals(DownloadStatus.FAILED, converters.toDownloadStatus("INVALID"))
        assertEquals(MediaType.VIDEO, converters.toMediaType("INVALID"))
    }

    @Test
    fun userSettings_defaultsValid() {
        val settings = UserSettings()

        assertEquals(2, settings.maxParallelDownloads)
        assertEquals("STABLE", settings.updateChannel)
        assertEquals("mp4", settings.defaultVideoFormat)
        assertEquals("mp3", settings.defaultAudioFormat)
        assertTrue(settings.embedMetadata)
        assertTrue(settings.embedThumbnail)
        assertFalse(settings.useAria2c)
    }

    @Test
    fun streamFormat_videoAndAudioFlags() {
        val videoFormat = StreamFormat(
            formatId = "137",
            ext = "mp4",
            resolution = "1080p",
            note = "1080p 60fps",
            filesize = 1024000L,
            isVideo = true,
            isAudio = false,
            vcodec = "avc1.640028",
            acodec = "none"
        )

        assertTrue(videoFormat.isVideo)
        assertFalse(videoFormat.isAudio)

        val streamInfo = StreamInfo(
            id = "vid123",
            title = "Sample Title",
            thumbnailUrl = "https://example.com/thumb.jpg",
            duration = 120L,
            uploader = "Channel Name",
            url = "https://example.com/watch?v=vid123",
            formats = listOf(videoFormat)
        )

        assertEquals(1, streamInfo.formats.size)
        assertEquals("vid123", streamInfo.id)
    }
}
