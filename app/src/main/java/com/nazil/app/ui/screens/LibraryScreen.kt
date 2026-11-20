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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nazil.app.data.DownloadStatus
import com.nazil.app.data.MediaFilter
import com.nazil.app.data.MediaKind
import com.nazil.app.data.formatBytes
import com.nazil.app.data.formatDuration
import com.nazil.app.ui.MainViewModel
import com.nazil.app.ui.components.DownloadRow
import com.nazil.app.ui.theme.Amber
import com.nazil.app.ui.theme.Indigo900
import com.nazil.app.ui.theme.Rose
import com.nazil.app.util.FileActions

@Composable
fun LibraryScreen(
    vm: MainViewModel,
    contentPadding: PaddingValues,
) {
    val context = LocalContext.current
    val history by vm.history.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(MediaFilter.ALL) }

    val filtered = remember(history, query, activeFilter) {
        history
            .asSequence()
            .filterNot { it.isActive } // Library only shows finished items
            .filter { matchesFilter(it, activeFilter) }
            .filter { matchesQuery(it, query) }
            .toList()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            LibraryHeader(
                stats = stats,
                usedSpace = vm.usedSpace(),
                onClearCompleted = vm::clearCompleted,
                onClearFailed = vm::clearFailed,
                onClearAll = vm::clearAll,
            )
        }

        item {
            SearchBox(
                value = query,
                onChange = { query = it },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item {
            FilterChips(
                selected = activeFilter,
                onSelect = { activeFilter = it },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (filtered.isEmpty()) {
            item {
                EmptyState(hasHistory = history.isNotEmpty())
            }
        } else {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${filtered.size} عنصر",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            items(filtered, key = { it.id }) { item ->
                DownloadRow(
                    item = item,
                    onClick = {
                        item.filePath?.let { FileActions.open(context, it) }
                    },
                    trailing = {
                        Row {
                            if (item.status == DownloadStatus.FAILED) {
                                IconButton(onClick = { vm.retry(item) }) {
                                    Icon(Icons.Filled.Refresh, contentDescription = "إعادة", Modifier.size(20.dp))
                                }
                            }
                            IconButton(onClick = {
                                item.filePath?.let { FileActions.share(context, it) }
                            }) {
                                Icon(Icons.Filled.Share, contentDescription = "مشاركة", Modifier.size(20.dp))
                            }
                            IconButton(onClick = { vm.delete(item) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "حذف", Modifier.size(20.dp))
                            }
                        }
                    },
                )
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

private fun matchesFilter(
    item: com.nazil.app.data.DownloadItem,
    filter: String,
): Boolean = when (filter) {
    MediaFilter.ALL -> true
    MediaFilter.VIDEO -> item.kind == MediaKind.VIDEO
    MediaFilter.AUDIO -> item.kind == MediaKind.AUDIO
    MediaFilter.IMAGE -> item.kind == MediaKind.IMAGE
    MediaFilter.COMPLETED -> item.status == DownloadStatus.COMPLETED
    MediaFilter.FAILED -> item.status == DownloadStatus.FAILED
    else -> true
}

private fun matchesQuery(
    item: com.nazil.app.data.DownloadItem,
    query: String,
): Boolean {
    val q = query.trim()
    if (q.isEmpty()) return true
    val words = q.lowercase().split(Regex("\\s+"))
    val hay = listOf(item.title, item.site, item.uploader ?: "", item.formatLabel)
        .joinToString(" ")
        .lowercase()
    return words.all { w -> hay.contains(w) }
}

@Composable
private fun LibraryHeader(
    stats: com.nazil.app.ui.DownloadStats,
    usedSpace: Long,
    onClearCompleted: () -> Unit,
    onClearFailed: () -> Unit,
    onClearAll: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Brush.linearGradient(listOf(Indigo900, Rose.copy(alpha = 0.85f))))
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "المكتبة",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Amber,
                )
                Spacer(Modifier.weight(1f))
                if (stats.total > 0) {
                    TextButton(onClick = onClearAll) {
                        Text("مسح الكل", color = MaterialTheme.colorScheme.surface)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                HeaderStat(Icons.Filled.CheckCircle, "مكتمل", "${stats.completed}", Amber)
                HeaderStat(Icons.Filled.Error, "فشل", "${stats.failed}", Rose)
                HeaderStat(Icons.Filled.PlayCircle, "المجموع", "${stats.total}", Amber)
                HeaderStat(Icons.Filled.Delete, "المساحة", formatBytes(usedSpace), Amber)
            }
            if (stats.completed > 0 || stats.failed > 0) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (stats.completed > 0) {
                        MiniButton("مسح المكتمل", onClearCompleted)
                    }
                    if (stats.failed > 0) {
                        MiniButton("مسح الفاشل", onClearFailed)
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniButton(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun HeaderStat(icon: ImageVector, label: String, value: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
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
private fun SearchBox(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("ابحث بالعنوان، الموقع، أو المنشئ…") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "مسح")
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
    )
}

@Composable
private fun FilterChips(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(MediaFilter.all) { f ->
            FilterChip(
                selected = selected == f,
                onClick = { onSelect(f) },
                label = { Text(MediaFilter.label(f)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}

@Composable
private fun EmptyState(hasHistory: Boolean) {
    Box(Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                if (hasHistory) "ماكو نتائج بهذا الفلتر" else "ماكو تنزيلات بعد.\nروح لتبويب «تحميل» والصق رابط.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
