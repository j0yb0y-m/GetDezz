package com.j0yb0y.getdezz.share

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.j0yb0y.getdezz.GetDezzApp
import com.j0yb0y.getdezz.ui.DownloadOptionsBottomSheet
import com.j0yb0y.getdezz.ui.MainViewModel
import com.j0yb0y.getdezz.ui.theme.GetDezzTheme

class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = extractUrlFromIntent(intent)
        if (sharedText.isNullOrEmpty()) {
            Toast.makeText(this, "No valid link shared", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val app = application as GetDezzApp
        val viewModel = ViewModelProvider(this, MainViewModel.Factory(app))[MainViewModel::class.java]

        viewModel.fetchStreamInfo(sharedText)

        setContent {
            GetDezzTheme {
                val isFetching by viewModel.isFetching.collectAsState()
                val streamInfo by viewModel.streamInfo.collectAsState()
                val fetchError by viewModel.fetchError.collectAsState()

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFetching) {
                        CircularProgressIndicator()
                    }

                    if (!fetchError.isNullOrEmpty()) {
                        Toast.makeText(this@ShareReceiverActivity, fetchError, Toast.LENGTH_LONG).show()
                        finish()
                    }

                    streamInfo?.let { info ->
                        DownloadOptionsBottomSheet(
                            streamInfo = info,
                            onDismiss = { finish() },
                            onConfirm = { mediaType, format, quality ->
                                viewModel.startDownload(
                                    context = this@ShareReceiverActivity,
                                    url = info.url,
                                    title = info.title,
                                    thumbnailUrl = info.thumbnailUrl,
                                    duration = info.duration,
                                    uploader = info.uploader,
                                    mediaType = mediaType,
                                    format = format,
                                    quality = quality
                                )
                                Toast.makeText(this@ShareReceiverActivity, "Download started", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun extractUrlFromIntent(intent: Intent?): String? {
        if (intent == null || intent.action != Intent.ACTION_SEND) return null
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
        val urlRegex = Regex("(https?://[\\w-]+(\\.[\\w-]+)+[/#?]?\\S*)")
        return urlRegex.find(text)?.value ?: text
    }
}
