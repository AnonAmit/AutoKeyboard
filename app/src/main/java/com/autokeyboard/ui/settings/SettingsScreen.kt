package com.autokeyboard.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.autokeyboard.data.provider.ProviderRegistry
import com.autokeyboard.ui.theme.*

/**
 * Settings screen — pixel-matched to settings_ai_config/code.html.
 * Sections: AI Providers grid, Provider config, Privacy, Appearance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val providerInfoList by viewModel.providerInfoList.collectAsState()
    val currentTheme by viewModel.theme.collectAsState()
    val haptic by viewModel.hapticEnabled.collectAsState()
    val stats by viewModel.anonymousStats.collectAsState()
    var expandedProviderId by remember { mutableStateOf<String?>(null) }

    androidx.activity.compose.BackHandler(enabled = expandedProviderId != null) {
        expandedProviderId = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Top Bar with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(SurfaceContainerLow, Background)
                    )
                )
                .padding(top = 48.dp, bottom = 24.dp)
                .padding(horizontal = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (expandedProviderId != null) {
                        expandedProviderId = null
                    } else {
                        onBack()
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = OnSurface)
                }
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = OnSurface
                    )
                    Text(
                        "Configure AI providers and preferences",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // ═══ Section A: AI Providers Grid ═══
            SectionHeader(emoji = "🤖", title = "AI PROVIDERS")

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(360.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(providerInfoList.filter { !it.isLocal }) { info ->
                    ProviderCard(
                        info = info,
                        isExpanded = expandedProviderId == info.id,
                        onClick = {
                            expandedProviderId = if (expandedProviderId == info.id) null else info.id
                        }
                    )
                }
            }

            // Expanded provider config
            expandedProviderId?.let { providerId ->
                ProviderConfigSection(
                    providerId = providerId,
                    viewModel = viewModel,
                    onClose = { expandedProviderId = null }
                )
            }

            // ═══ Section B: Local Models ═══
            SectionHeader(emoji = "📱", title = "ON-DEVICE MODELS")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                providerInfoList.filter { it.isLocal }.forEach { info ->
                    LocalModelCard(info = info)
                }
            }

            // ═══ Section C: Privacy ═══
            SectionHeader(emoji = "🔒", title = "PRIVACY")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainer, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsToggle(
                    title = "Anonymous Usage Stats",
                    subtitle = "Help improve AutoKeyboard",
                    isChecked = stats,
                    onToggle = { viewModel.setAnonymousStats(it) }
                )
                Divider(color = Color.White.copy(alpha = 0.05f))
                SettingsToggle(
                    title = "Haptic Feedback",
                    subtitle = "Vibrate on key press",
                    isChecked = haptic,
                    onToggle = { viewModel.setHapticEnabled(it) }
                )
            }

            // ═══ Section D: Appearance ═══
            SectionHeader(emoji = "🎨", title = "APPEARANCE")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    ThemeCard(
                        theme = theme,
                        isSelected = theme == currentTheme,
                        onClick = { viewModel.setTheme(theme) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(emoji: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(emoji, fontSize = 18.sp)
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun ProviderCard(
    info: ProviderRegistry.ProviderInfo,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isExpanded) SurfaceContainerHigh
                else SurfaceContainer
            )
            .border(
                if (info.isConfigured) 1.dp else 0.dp,
                if (info.isConfigured) Secondary.copy(alpha = 0.3f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(info.displayName, style = MaterialTheme.typography.titleSmall, color = OnSurface)
                if (info.isConfigured) {
                    Box(
                        modifier = Modifier
                            .background(Secondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "CONNECTED",
                            fontSize = 8.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = Secondary,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
            Text(info.description, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        }
    }
}

@Composable
private fun ProviderConfigSection(
    providerId: String,
    viewModel: SettingsViewModel,
    onClose: () -> Unit
) {
    var apiKeyInput by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    val hasKey = viewModel.hasApiKey(providerId)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainerHigh, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Configure ${providerId.replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.titleSmall, color = OnSurface)
            Icon(Icons.Filled.Close, "Close", tint = OnSurfaceVariant, modifier = Modifier.clickable { onClose() })
        }

        // API Key field
        OutlinedTextField(
            value = apiKeyInput,
            onValueChange = { apiKeyInput = it },
            label = { Text("API Key") },
            placeholder = { Text(if (hasKey) viewModel.getApiKeyMasked(providerId) else "sk-...") },
            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showKey = !showKey }) {
                    Icon(
                        if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        "Toggle visibility"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = OutlineVariant.copy(alpha = 0.3f),
                focusedBorderColor = Primary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Save / Delete buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    if (apiKeyInput.isNotBlank()) {
                        viewModel.saveApiKey(providerId, apiKeyInput)
                        apiKeyInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Key", color = OnPrimary)
            }
            if (hasKey) {
                OutlinedButton(
                    onClick = { viewModel.deleteApiKey(providerId) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun LocalModelCard(info: ProviderRegistry.ProviderInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainer, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(info.displayName, style = MaterialTheme.typography.titleSmall, color = OnSurface)
            Text(info.description, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        }
        Icon(Icons.Filled.Download, "Download", tint = Primary)
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = OnSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = OnPrimary,
                checkedTrackColor = Primary,
                uncheckedTrackColor = SurfaceContainerHighest
            )
        )
    }
}

@Composable
private fun ThemeCard(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = when (theme) {
        AppTheme.SYSTEM -> "System"
        AppTheme.DARK -> "Dark"
        AppTheme.OLED -> "OLED"
    }
    val icon = when (theme) {
        AppTheme.SYSTEM -> Icons.Filled.SettingsBrightness
        AppTheme.DARK -> Icons.Filled.DarkMode
        AppTheme.OLED -> Icons.Filled.Contrast
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) SurfaceContainerHigh else SurfaceContainer
            )
            .border(
                if (isSelected) 1.5.dp else 0.dp,
                if (isSelected) Primary else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, name, tint = if (isSelected) Primary else OnSurfaceVariant, modifier = Modifier.size(28.dp))
        Text(
            name,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) Primary else OnSurfaceVariant
        )
    }
}
