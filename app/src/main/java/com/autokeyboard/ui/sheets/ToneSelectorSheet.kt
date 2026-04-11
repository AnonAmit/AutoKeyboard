package com.autokeyboard.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autokeyboard.data.model.Tone
import com.autokeyboard.ui.theme.*

/**
 * Tone Selector Bottom Sheet — pixel-matched to tone_selector_panel/code.html.
 * 60% height, bg #1d1d37, rounded-t-24dp, 2-column grid of 8 tone chips.
 */
@Composable
fun ToneSelectorSheet(
    currentTone: Tone,
    onToneSelected: (Tone) -> Unit,
    onCustomPromptChanged: (String) -> Unit,
    customPrompt: String,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .background(
                SurfaceContainerHigh,
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .background(
                        OutlineVariant.copy(alpha = 0.2f),
                        RoundedCornerShape(9999.dp)
                    )
            )
        }

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Text(
                text = "Select Voice Tone",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface
            )
            Text(
                text = "Personalize how your AI assistant writes.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Tone grid — 2 columns
            val tones = Tone.entries.filter { it != Tone.CUSTOM }
            val rows = tones.chunked(2)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rows.forEach { rowTones ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowTones.forEach { tone ->
                            ToneChip(
                                tone = tone,
                                isSelected = tone == currentTone,
                                onClick = { onToneSelected(tone) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // If odd number, add spacer
                        if (rowTones.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Custom Prompt Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.Add,
                    contentDescription = null,
                    tint = PrimaryFixedDim,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "CUSTOM PROMPT",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryFixedDim,
                    letterSpacing = 2.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // Custom prompt input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        SurfaceContainerHighest.copy(alpha = 0.5f),
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        1.dp,
                        OutlineVariant.copy(alpha = 0.15f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                if (customPrompt.isEmpty()) {
                    Text(
                        text = "Talk like my GF...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant.copy(alpha = 0.4f),
                        fontStyle = FontStyle.Italic
                    )
                }
                // Basic text field
                androidx.compose.foundation.text.BasicTextField(
                    value = customPrompt,
                    onValueChange = onCustomPromptChanged,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = OnSurface,
                        fontStyle = FontStyle.Italic
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Apply button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceContainerHigh)
                .padding(24.dp, 8.dp, 24.dp, 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        Brush.linearGradient(listOf(Primary, PrimaryDim)),
                        RoundedCornerShape(9999.dp)
                    )
                    .clickable { onApply() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Apply Tone",
                    style = MaterialTheme.typography.labelLarge,
                    color = OnPrimary
                )
            }
        }
    }
}

/**
 * Individual tone chip — unselected: border primary/40, selected: gradient fill.
 */
@Composable
private fun ToneChip(
    tone: Tone,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .then(
                if (isSelected) {
                    Modifier.background(
                        Brush.linearGradient(listOf(Primary, PrimaryDim)),
                        RoundedCornerShape(9999.dp)
                    )
                } else {
                    Modifier.border(
                        1.dp,
                        Primary.copy(alpha = 0.4f),
                        RoundedCornerShape(9999.dp)
                    )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tone.displayName,
            fontSize = 14.sp,
            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold
            else androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = if (isSelected) OnPrimary else Primary
        )
    }
}
