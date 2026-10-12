package com.nazil.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nazil.app.data.DownloadItem
import com.nazil.app.ui.MainViewModel
import com.nazil.app.ui.components.DownloadRow
import com.nazil.app.util.FileActions

@Composable
fun LibraryScreen(
    vm: MainViewModel,
    contentPadding: PaddingValues,
) {
    val context = LocalContext.current
    val history by vm.history.collectAsStateWithLifecycle()
    val finished = history.filterNot { it.isActive }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "المكتبة",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (finished.isNotEmpty()) {
                    TextButton(onClick = vm::clearCompleted) {
                        Text("مسح الكل")
                    }
                }
            }
        }

        if (finished.isEmpty()) {
            item {
                Box(Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "ماكو تنزيلات بعد.\nروح لتبويب «تحميل» والصق رابط.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        items(finished, key = { it.id }) { item ->
            DownloadRow(
                item = item,
                onClick = {
                    item.filePath?.let { FileActions.open(context, it) }
                },
                trailing = {
                    Row {
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
        item { Spacer(Modifier.height(8.dp)) }
    }
}
