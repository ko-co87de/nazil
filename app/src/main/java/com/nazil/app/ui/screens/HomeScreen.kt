package com.nazil.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nazil.app.data.Quality
import com.nazil.app.ui.MainViewModel
import com.nazil.app.ui.components.DownloadRow
import com.nazil.app.ui.components.MediaThumb
import com.nazil.app.ui.components.SitePill
import com.nazil.app.ui.theme.Amber
import com.nazil.app.ui.theme.Indigo900
import com.nazil.app.ui.theme.Rose

@Composable
fun HomeScreen(
    vm: MainViewModel,
    contentPadding: PaddingValues,
) {
    val clipboard = LocalClipboardManager.current
    val history by vm.history.collectAsStateWithLifecycle()
    val active = history.filter { it.isActive }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Header()
        }

        item {
            LinkCard(
                value = vm.urlInput,
                onValueChange = vm::onUrlChange,
                onPaste = {
                    clipboard.getText()?.text?.let { vm.onUrlChange(it) }
                },
                onFetch = vm::fetchInfo,
                onDownload = { vm.startDownload() },
                isFetching = vm.isFetching,
            )
        }

        vm.fetchedInfo?.let { info ->
            item {
                PreviewCard(
                    title = info.title ?: "بدون عنوان",
                    thumbnail = info.thumbnail,
                    site = info.extractor ?: "?",
                    durationSeconds = (info.duration as? Number)?.toLong() ?: 0L,
                )
            }
        }

        item {
            QualityRow(
                selected = vm.currentQuality,
                onSelect = vm::setDefaultQuality,
            )
        }

        if (active.isNotEmpty()) {
            item { SectionTitle("يتحمّل هسه (${active.size})") }
            items(active, key = { it.id }) { item ->
                DownloadRow(
                    item = item,
                    onClick = {},
                    trailing = {
                        IconButton(onClick = { vm.cancel(item) }) {
                            Icon(Icons.Filled.Close, contentDescription = "إلغاء")
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun Header() {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Brush.linearGradient(listOf(Indigo900, Rose.copy(alpha = 0.9f))))
            .padding(horizontal = 20.dp, vertical = 26.dp),
    ) {
        Column {
            Text(
                "Nazil",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Amber,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "الصق رابط من أي منصة ونزّله فوراً",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.surface,
            )
        }
    }
}

@Composable
private fun LinkCard(
    value: String,
    onValueChange: (String) -> Unit,
    onPaste: () -> Unit,
    onFetch: () -> Unit,
    onDownload: () -> Unit,
    isFetching: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("رابط الفيديو أو الصورة") },
                shape = RoundedCornerShape(14.dp),
                singleLine = false,
                maxLines = 3,
                trailingIcon = {
                    IconButton(onClick = onPaste) {
                        Icon(Icons.Filled.ContentPaste, contentDescription = "لصق")
                    }
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onFetch,
                    enabled = !isFetching,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    if (isFetching) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Search, contentDescription = null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("جيب معلومات")
                    }
                }
                Button(
                    onClick = onDownload,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("نزّل")
                }
            }
        }
    }
}

@Composable
private fun PreviewCard(
    title: String,
    thumbnail: String?,
    site: String,
    durationSeconds: Long,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            MediaThumb(
                url = thumbnail,
                kind = com.nazil.app.data.MediaKind.VIDEO,
                modifier = Modifier.fillMaxWidth().height(180.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SitePill(site)
                if (durationSeconds > 0) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        formatDuration(durationSeconds),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun QualityRow(selected: String, onSelect: (String) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("الجودة")
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Quality.all.forEach { q ->
                FilterChip(
                    selected = selected == q,
                    onClick = { onSelect(q) },
                    label = { Text(Quality.label(q)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
