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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nazil.app.data.MediaKind
import com.nazil.app.data.Quality
import com.nazil.app.data.formatBytes
import com.nazil.app.data.formatDuration
import com.nazil.app.ui.MainViewModel
import com.nazil.app.ui.components.DownloadRow
import com.nazil.app.ui.components.FormatPickerDialog
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
    val stats by vm.stats.collectAsStateWithLifecycle()
    val active = history.filter { it.isActive }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Header(stats = stats, totalBytes = vm.usedSpace())
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
                    availableFormatsCount = vm.availableFormats.size,
                    onPickFormat = vm::openFormatPicker,
                )
            }
        }

        item {
            QualityRow(
                selected = vm.currentQuality,
                customLabel = vm.customFormat?.label,
                onSelect = vm::setDefaultQuality,
                onPickCustom = vm::openFormatPicker,
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

    if (vm.showFormatPicker) {
        FormatPickerDialog(
            formats = vm.availableFormats,
            onDismiss = vm::closeFormatPicker,
            onSelect = vm::selectCustomFormat,
        )
    }
}

@Composable
private fun Header(
    stats: com.nazil.app.ui.DownloadStats,
    totalBytes: Long,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Brush.linearGradient(listOf(Indigo900, Rose.copy(alpha = 0.9f))))
            .padding(horizontal = 20.dp, vertical = 22.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(Amber, Rose))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "ن",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Indigo900,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Nazil",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Amber,
                    )
                    Text(
                        "الصق رابط من أي منصة ونزّله فوراً",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.surface,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatItem(
                    icon = Icons.Filled.Schedule,
                    label = "نشط",
                    value = "${stats.active}",
                )
                StatItem(
                    icon = Icons.Filled.CheckCircle,
                    label = "مكتمل",
                    value = "${stats.completed}",
                )
                StatItem(
                    icon = Icons.Filled.Download,
                    label = "المجموع",
                    value = "${stats.total}",
                )
                StatItem(
                    icon = Icons.Filled.PlayCircle,
                    label = "الحجم",
                    value = formatBytes(totalBytes),
                )
            }
        }
    }
}

@Composable
private fun StatItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Amber, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.surface,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        )
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
                placeholder = { Text("https://youtube.com/watch?v=...") },
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
    availableFormatsCount: Int,
    onPickFormat: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Box {
                MediaThumb(
                    url = thumbnail,
                    kind = MediaKind.VIDEO,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                )
                if (durationSeconds > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xCC000000),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                    ) {
                        Text(
                            formatDuration(durationSeconds),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SitePill(site)
                    Spacer(Modifier.width(8.dp))
                    if (availableFormatsCount > 0) {
                        Text(
                            "${availableFormatsCount} صيغة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (availableFormatsCount > 0) {
                    OutlinedButton(
                        onClick = onPickFormat,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Icon(Icons.Filled.Tune, contentDescription = null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("اختر صيغة")
                    }
                }
            }
        }
    }
}

@Composable
private fun QualityRow(
    selected: String,
    customLabel: String?,
    onSelect: (String) -> Unit,
    onPickCustom: () -> Unit,
) {
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
            FilterChip(
                selected = selected == Quality.CUSTOM,
                onClick = onPickCustom,
                label = { Text(customLabel ?: "صيغة محددة") },
                leadingIcon = {
                    Icon(Icons.Filled.Tune, contentDescription = null, Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                ),
            )
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
