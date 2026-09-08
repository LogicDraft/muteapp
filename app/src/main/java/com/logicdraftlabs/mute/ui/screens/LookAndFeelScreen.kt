package com.logicdraftlabs.mute.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.logicdraftlabs.mute.data.PrefsManager
import com.logicdraftlabs.mute.ui.viewmodel.MainViewModel

@Composable
fun LookAndFeelScreen(viewModel: MainViewModel, onBackClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val theme by viewModel.themePreference.collectAsState()
    val dynamicColors by viewModel.dynamicColorsEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("Look & Feel", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            Text("Make MUTO feel at home on your phone.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

            SilentPalettePreview()
            Text("COLOR MOODS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PaletteSwatch(listOf(Color(0xFF87C8F5), Color(0xFF1E4358), Color(0xFFB8D9F1), Color(0xFF406A83)), selected = true)
                PaletteSwatch(listOf(Color(0xFF9BD5B2), Color(0xFF204D38), Color(0xFFC9E8D2), Color(0xFF5F8B70)))
                PaletteSwatch(listOf(Color(0xFFF2B36B), Color(0xFF674017), Color(0xFFFAD7AC), Color(0xFF94704A)))
                PaletteSwatch(listOf(Color(0xFFE19AAB), Color(0xFF5F2432), Color(0xFFF1C9D2), Color(0xFF89616A)))
            }
            Text("MUTO follows your wallpaper when Dynamic colors is enabled.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            AppearanceCard(
                title = "Dynamic colors",
                subtitle = "Use colors from your device wallpaper",
                icon = Icons.Outlined.Wallpaper,
                trailing = { Switch(checked = dynamicColors, onCheckedChange = { viewModel.setDynamicColors(context, it) }) }
            )

            Text("ADDITIONAL SETTINGS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            AppearanceCard(
                title = "Theme",
                subtitle = "Choose how MUTO looks",
                icon = Icons.Outlined.DarkMode,
                trailing = {}
            ) {
                ThemeOption("System", PrefsManager.THEME_SYSTEM, theme) { viewModel.setThemePreference(context, it) }
                ThemeOption("Light", PrefsManager.THEME_LIGHT, theme) { viewModel.setThemePreference(context, it) }
                ThemeOption("Dark", PrefsManager.THEME_DARK, theme) { viewModel.setThemePreference(context, it) }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SilentPalettePreview() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh), shape = MaterialTheme.shapes.extraLarge) {
        Row(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(76.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(68.dp)) {
                    drawCircle(MaterialTheme.colorScheme.primary.copy(alpha = .18f))
                    drawCircle(MaterialTheme.colorScheme.primary, style = Stroke(width = 3.dp.toPx()))
                    drawCircle(MaterialTheme.colorScheme.primary, radius = 8.dp.toPx())
                }
            }
            Spacer(Modifier.width(18.dp))
            Column {
                Text("Quiet by design", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Soft surfaces. One intentional accent.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PaletteSwatch(colors: List<Color>, selected: Boolean = false) {
    Box(
        modifier = Modifier.size(66.dp).clip(RoundedCornerShape(18.dp)).then(
            if (selected) Modifier else Modifier
        ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(54.dp)) {
            val radius = size.minDimension / 2f
            colors.forEachIndexed { index, color -> drawArc(color, index * 90f, 90f, true) }
            if (selected) drawCircle(Color.White.copy(alpha = .8f), radius = radius, style = Stroke(2.dp.toPx()))
        }
    }
}

@Composable
private fun AppearanceCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: @Composable () -> Unit,
    content: @Composable (() -> Unit)? = null
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), shape = MaterialTheme.shapes.extraLarge) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                trailing()
            }
            content?.let { block -> Spacer(Modifier.height(12.dp)); block() }
        }
    }
}

@Composable
private fun ThemeOption(label: String, value: String, selected: String, onSelect: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onSelect(value) }, verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected == value, onClick = { onSelect(value) })
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
