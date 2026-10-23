package com.j0yb0y.getdezz.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.j0yb0y.getdezz.R
import com.j0yb0y.getdezz.data.MediaType
import com.j0yb0y.getdezz.engine.StreamInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadOptionsBottomSheet(
    streamInfo: StreamInfo,
    onDismiss: () -> Unit,
    onConfirm: (mediaType: MediaType, format: String, quality: String) -> Unit
) {
    var selectedMediaType by remember { mutableStateOf(MediaType.VIDEO) }

    val videoFormats = listOf("mp4", "mkv", "webm")
    val audioFormats = listOf("mp3", "m4a", "opus", "flac", "wav")

    val videoQualities = listOf("1080p", "720p", "480p", "360p", "2160p")
    val audioQualities = listOf("320k", "256k", "192k", "128k")

    var selectedVideoFormat by remember { mutableStateOf("mp4") }
    var selectedAudioFormat by remember { mutableStateOf("mp3") }

    var selectedVideoQuality by remember { mutableStateOf("1080p") }
    var selectedAudioQuality by remember { mutableStateOf("320k") }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!streamInfo.thumbnailUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = streamInfo.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(100.dp)
                            .height(60.dp)
                            .clip(MaterialTheme.shapes.small)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = streamInfo.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!streamInfo.uploader.isNullOrEmpty()) {
                        Text(
                            text = streamInfo.uploader,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedMediaType == MediaType.VIDEO,
                    onClick = { selectedMediaType = MediaType.VIDEO },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(stringResource(R.string.format_video))
                }
                SegmentedButton(
                    selected = selectedMediaType == MediaType.AUDIO,
                    onClick = { selectedMediaType = MediaType.AUDIO },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(stringResource(R.string.format_audio))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.label_format),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                val formats = if (selectedMediaType == MediaType.VIDEO) videoFormats else audioFormats
                val currentFormat = if (selectedMediaType == MediaType.VIDEO) selectedVideoFormat else selectedAudioFormat

                formats.forEach { fmt ->
                    FilterChip(
                        selected = currentFormat == fmt,
                        onClick = {
                            if (selectedMediaType == MediaType.VIDEO) selectedVideoFormat = fmt
                            else selectedAudioFormat = fmt
                        },
                        label = { Text(fmt.uppercase()) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.label_quality),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                val qualities = if (selectedMediaType == MediaType.VIDEO) videoQualities else audioQualities
                val currentQuality = if (selectedMediaType == MediaType.VIDEO) selectedVideoQuality else selectedAudioQuality

                qualities.forEach { q ->
                    FilterChip(
                        selected = currentQuality == q,
                        onClick = {
                            if (selectedMediaType == MediaType.VIDEO) selectedVideoQuality = q
                            else selectedAudioQuality = q
                        },
                        label = { Text(q) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val fmt = if (selectedMediaType == MediaType.VIDEO) selectedVideoFormat else selectedAudioFormat
                    val qual = if (selectedMediaType == MediaType.VIDEO) selectedVideoQuality else selectedAudioQuality
                    onConfirm(selectedMediaType, fmt, qual)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_start_download))
            }
        }
    }
}
