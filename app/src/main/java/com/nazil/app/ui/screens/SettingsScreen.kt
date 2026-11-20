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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nazil.app.BuildConfig
import com.nazil.app.data.Quality
import com.nazil.app.data.ThemeMode
import com.nazil.app.ui.MainViewModel
import com.nazil.app.ui.theme.Amber
import com.nazil.app.ui.theme.Indigo900
import com.nazil.app.ui.theme.Rose

@Composable
fun SettingsScreen(
    vm: MainViewModel,
    contentPadding: PaddingValues,
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var subtitleLangs by remember(settings.subtitleLangs) { mutableStateOf(settings.subtitleLangs) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SettingsHeader() }

        item {
            SettingsCard(
                title = "المظهر",
                icon = Icons.Filled.Palette,
                content = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.all.forEach { mode ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = { vm.setTheme(mode) },
                                label = { Text(themeLabel(mode)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            )
                        }
                    }
                },
            )
        }

        item {
            SettingsCard(
                title = "الجودة الافتراضية",
                icon = Icons.Filled.HighQuality,
                content = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Quality.all.forEach { q ->
                            FilterChip(
                                selected = vm.currentQuality == q,
                                onClick = { vm.setDefaultQuality(q) },
                                label = { Text(Quality.label(q)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            )
                        }
                    }
                },
            )
        }

        item {
            SettingsCard(
                title = "SponsorBlock",
                subtitle = "شيل مقاطع الإعلانات والترويج من فيديوهات يوتيوب تلقائياً",
                icon = Icons.Filled.Block,
                content = {
                    SwitchRow(
                        checked = settings.sponsorBlock,
                        onChange = vm::setSponsorBlock,
                        label = "تفعيل SponsorBlock",
                    )
                },
            )
        }

        item {
            SettingsCard(
                title = "الترجمة (Subtitles)",
                subtitle = "نزّل ترجمة تلقائية مع الفيديو وادمجها بداخل ملف MP4",
                icon = Icons.Filled.Subscriptions,
                content = {
                    SwitchRow(
                        checked = settings.downloadSubtitles,
                        onChange = { vm.setSubtitles(it, subtitleLangs) },
                        label = "تنزيل الترجمة",
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = subtitleLangs,
                        onValueChange = {
                            subtitleLangs = it
                            if (settings.downloadSubtitles) vm.setSubtitles(true, it)
                        },
                        label = { Text("أكواد اللغات") },
                        placeholder = { Text("ar,en,es") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        "أكواد اللغات مفصولة بفاصلة. مثال: ar للعربية، en للإنجليزية.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                },
            )
        }

        item {
            SettingsCard(
                title = "الشبكة",
                icon = Icons.Filled.Wifi,
                content = {
                    SwitchRow(
                        checked = settings.downloadOverWifiOnly,
                        onChange = vm::setWifiOnly,
                        label = "نزّل على واي فاي فقط",
                    )
                    Spacer(Modifier.height(8.dp))
                    SwitchRow(
                        checked = settings.autoRetry,
                        onChange = vm::setAutoRetry,
                        label = "إعادة المحاولة تلقائياً عند الفشل",
                    )
                },
            )
        }

        item {
            SettingsCard(
                title = "التنزيلات المتزامنة",
                subtitle = "عدد التحميلات اللي تشتغل بنفس الوقت",
                icon = Icons.Filled.GraphicEq,
                content = {
                    Slider(
                        value = settings.maxConcurrent.toFloat(),
                        onValueChange = { vm.setConcurrent(it.toInt()) },
                        valueRange = 1f..4f,
                        steps = 2,
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("1", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "${settings.maxConcurrent} متزامن",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text("4", style = MaterialTheme.typography.labelSmall)
                    }
                },
            )
        }

        item {
            SettingsCard(
                title = "مجلد التنزيل",
                icon = Icons.Filled.Folder,
                content = {
                    Text(
                        "Download/Nazil",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "الملفات تنحفظ بمجلد التنزيلات العام على الذاكرة الخارجية، تكدر تفتحها من أي تطبيق معرض أو مشغل.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }

        item {
            SettingsCard(
                title = "حول",
                icon = Icons.Filled.Info,
                content = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(Amber, Rose))),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("ن", color = Indigo900, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Nazil",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "الإصدار ${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "مبني على yt-dlp — يدعم 1000+ منصة فيديو وصوت وصور.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.BugReport, contentDescription = null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "صنع بحب 🇮🇶 — بغداد",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun SettingsHeader() {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Brush.linearGradient(listOf(Indigo900, Rose.copy(alpha = 0.85f))))
            .padding(horizontal = 20.dp, vertical = 22.dp),
    ) {
        Column {
            Text(
                "الإعدادات",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Amber,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "خصّص تجربتك مع Nazil",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.surface,
            )
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (subtitle != null) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun SwitchRow(
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    label: String,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

private fun themeLabel(mode: String): String = when (mode) {
    ThemeMode.LIGHT -> "فاتح"
    ThemeMode.DARK -> "داكن"
    else -> "تلقائي"
}
