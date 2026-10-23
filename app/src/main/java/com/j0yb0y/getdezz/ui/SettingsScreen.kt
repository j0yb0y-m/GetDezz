package com.j0yb0y.getdezz.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.j0yb0y.getdezz.R

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val userSettings by viewModel.userSettings.collectAsState(initial = null)
    val updateStatus by viewModel.updateStatus.collectAsState()

    val settings = userSettings ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.setting_max_parallel),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "${settings.maxParallelDownloads}",
            style = MaterialTheme.typography.bodyLarge
        )
        Slider(
            value = settings.maxParallelDownloads.toFloat(),
            onValueChange = { viewModel.updateMaxParallelDownloads(it.toInt()) },
            valueRange = 1f..4f,
            steps = 2
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.setting_embed_metadata),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = settings.embedMetadata,
                onCheckedChange = { viewModel.updateEmbedMetadata(it) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.setting_embed_thumbnail),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = settings.embedThumbnail,
                onCheckedChange = { viewModel.updateEmbedThumbnail(it) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.setting_use_aria2c),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = settings.useAria2c,
                onCheckedChange = { viewModel.updateUseAria2c(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.updateYtDlp(settings.updateChannel) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_check_update))
        }

        if (!updateStatus.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = updateStatus!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
