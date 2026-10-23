package com.j0yb0y.getdezz.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.j0yb0y.getdezz.R
import com.j0yb0y.getdezz.data.DownloadStatus
import com.j0yb0y.getdezz.engine.EngineState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTab by remember { mutableIntStateOf(0) }
    val urlInput by viewModel.urlInput.collectAsState()
    val isFetching by viewModel.isFetching.collectAsState()
    val streamInfo by viewModel.streamInfo.collectAsState()
    val fetchError by viewModel.fetchError.collectAsState()
    val engineState by viewModel.engineState.collectAsState()
    val downloads by viewModel.allDownloads.collectAsState(initial = emptyList())

    var previewFilePath by remember { mutableStateOf<String?>(null) }
    var previewTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.app_name))
                        Spacer(modifier = Modifier.width(12.dp))
                        val engineText = when (engineState) {
                            is EngineState.Ready -> stringResource(R.string.engine_ready)
                            is EngineState.Initializing -> stringResource(R.string.engine_initializing)
                            is EngineState.Failed -> stringResource(R.string.engine_failed)
                        }
                        Text(
                            text = "($engineText)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (engineState is EngineState.Ready) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Download, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_downloads)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_settings)) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (selectedTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { viewModel.onUrlInputChanged(it) },
                            placeholder = { Text(stringResource(R.string.url_hint)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                val clipText = clipboardManager.getText()?.text
                                if (!clipText.isNullOrEmpty()) {
                                    viewModel.onUrlInputChanged(clipText)
                                }
                            }
                        ) {
                            Text(stringResource(R.string.btn_paste))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.fetchStreamInfo() },
                            enabled = urlInput.isNotBlank() && !isFetching
                        ) {
                            if (isFetching) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .height(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(stringResource(R.string.btn_fetch))
                            }
                        }
                    }

                    if (!fetchError.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = fetchError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val activeDownloads = downloads.filter {
                        it.status in listOf(DownloadStatus.QUEUED, DownloadStatus.DOWNLOADING, DownloadStatus.PROCESSING)
                    }
                    val finishedDownloads = downloads.filter {
                        it.status in listOf(DownloadStatus.COMPLETED, DownloadStatus.FAILED, DownloadStatus.CANCELLED)
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (activeDownloads.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.section_active),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(activeDownloads, key = { it.id }) { item ->
                                DownloadItemCard(
                                    download = item,
                                    onCancel = { viewModel.cancelDownload(context, item.id) },
                                    onDelete = { viewModel.deleteDownload(item.id) },
                                    onPreview = {},
                                    onOpen = {}
                                )
                            }
                        }

                        if (finishedDownloads.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp, bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.section_history),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedButton(onClick = { viewModel.clearFinishedDownloads() }) {
                                        Text(stringResource(R.string.btn_clear_finished))
                                    }
                                }
                            }
                            items(finishedDownloads, key = { it.id }) { item ->
                                DownloadItemCard(
                                    download = item,
                                    onCancel = {},
                                    onDelete = { viewModel.deleteDownload(item.id) },
                                    onPreview = {
                                        item.filePath?.let { path ->
                                            previewFilePath = path
                                            previewTitle = item.title
                                        }
                                    },
                                    onOpen = {
                                        item.filePath?.let { path ->
                                            val uri = Uri.parse(path)
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(uri, if (item.mediaType.name == "VIDEO") "video/*" else "audio/*")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(intent, "Open with"))
                                        }
                                    }
                                )
                            }
                        }

                        if (downloads.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.empty_downloads),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                SettingsScreen(viewModel = viewModel)
            }

            streamInfo?.let { info ->
                DownloadOptionsBottomSheet(
                    streamInfo = info,
                    onDismiss = { viewModel.clearStreamInfo() },
                    onConfirm = { mediaType, format, quality ->
                        viewModel.startDownload(
                            context = context,
                            url = info.url,
                            title = info.title,
                            thumbnailUrl = info.thumbnailUrl,
                            duration = info.duration,
                            uploader = info.uploader,
                            mediaType = mediaType,
                            format = format,
                            quality = quality
                        )
                    }
                )
            }

            previewFilePath?.let { path ->
                PlayerDialog(
                    filePathOrUri = path,
                    title = previewTitle,
                    onDismiss = { previewFilePath = null }
                )
            }
        }
    }
}
