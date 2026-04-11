package com.autokeyboard.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autokeyboard.data.model.RewriteState
import com.autokeyboard.service.KeyboardViewModel
import com.autokeyboard.ui.sheets.RewriteResultsSheet
import com.autokeyboard.ui.sheets.ToneSelectorSheet
import com.autokeyboard.ui.theme.*

/**
 * Main keyboard composable — pixel-matched to keyboard_base_state/code.html.
 * Contains: AI Toolbar + QWERTY keys + bottom row.
 */
@Composable
fun KeyboardScreen(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onImprove: () -> Unit,
    onInsertRewrite: (String) -> Unit,
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier
) {
    val keyboardMode by viewModel.keyboardMode.collectAsState()
    val isShifted by viewModel.isShifted.collectAsState()
    val isCapsLock by viewModel.isCapsLock.collectAsState()
    val rewriteState by viewModel.rewriteState.collectAsState()
    val currentTone by viewModel.currentTone.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Background)
            .navigationBarsPadding() // Protects against overlap with system's gesture bar and IME switcher
    ) {
        // ═══ AI Toolbar Row ═══
        // bg-[#111127] border-t border-outline-variant/15
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceContainerLow)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Improve button (primary accent)
            ToolbarButton(
                icon = Icons.Filled.AutoFixHigh,
                label = "Improve",
                isPrimary = true,
                onClick = onImprove
            )
            // Emoji button
            ToolbarButton(
                icon = Icons.Filled.Mood,
                label = "Emoji",
                onClick = { viewModel.showEmojiStrip() }
            )
            // Tone button
            ToolbarButton(
                icon = Icons.Filled.Psychology,
                label = "Tone",
                onClick = { viewModel.showToneSelector() }
            )
        }

        // ═══ Keys & Overlays Container ═══
        Box(modifier = Modifier.fillMaxWidth()) {
            // ═══ Keyboard Keys ═══
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val shouldUpperCase = isShifted || isCapsLock

                // Row 1: Q W E R T Y U I O P
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p").forEach { key ->
                        val display = if (shouldUpperCase) key.uppercase() else key
                        KeyTile(
                            label = display,
                            onClick = {
                                onKeyPress(display)
                                viewModel.onKeyTyped()
                            }
                        )
                    }
                }

                // Row 2: A S D F G H J K L (with 5% horizontal padding)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("a", "s", "d", "f", "g", "h", "j", "k", "l").forEach { key ->
                        val display = if (shouldUpperCase) key.uppercase() else key
                        KeyTile(
                            label = display,
                            onClick = {
                                onKeyPress(display)
                                viewModel.onKeyTyped()
                            }
                        )
                    }
                }

                // Row 3: Shift Z X C V B N M Backspace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Shift key
                    KeyTile(
                        label = "",
                        isSpecial = true,
                        flex = 1.2f,
                        onClick = { viewModel.onShiftToggle() },
                        content = {
                            Icon(
                                imageVector = if (isCapsLock) Icons.Filled.KeyboardCapslock
                                else Icons.Filled.KeyboardArrowUp,
                                contentDescription = "Shift",
                                tint = if (isShifted || isCapsLock) Primary else OnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )

                    listOf("z", "x", "c", "v", "b", "n", "m").forEach { key ->
                        val display = if (shouldUpperCase) key.uppercase() else key
                        KeyTile(
                            label = display,
                            onClick = {
                                onKeyPress(display)
                                viewModel.onKeyTyped()
                            }
                        )
                    }

                    // Backspace key
                    KeyTile(
                        label = "",
                        isSpecial = true,
                        flex = 1.2f,
                        onClick = onBackspace,
                        content = {
                            Icon(
                                imageVector = Icons.Filled.Backspace,
                                contentDescription = "Backspace",
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }

                // Row 4: Mic ?123 Space . Send
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 4.dp, vertical = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Mic button
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                            .background(SurfaceContainerHighest, RoundedCornerShape(9999.dp))
                            .clickable { /* Voice input */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = "Mic",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // ?123 button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(SurfaceContainerHighest, RoundedCornerShape(9999.dp))
                            .clickable { viewModel.onNumberModeToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "?123",
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = OnSurfaceVariant
                        )
                    }

                    // Spacebar
                    Box(
                        modifier = Modifier
                            .weight(5f)
                            .fillMaxHeight()
                            .background(SurfaceContainerHighest, RoundedCornerShape(9999.dp))
                            .clickable { onKeyPress(" ") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "space",
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                            color = OnSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }

                    // Period
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(SurfaceContainerHighest, RoundedCornerShape(9999.dp))
                            .clickable { onKeyPress(".") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(".", fontSize = 18.sp, color = OnSurfaceVariant)
                    }

                    // Send button (gradient)
                    Box(
                        modifier = Modifier
                            .weight(2.5f)
                            .fillMaxHeight()
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(9999.dp),
                                ambientColor = Primary.copy(alpha = 0.3f)
                            )
                            .background(
                                Brush.linearGradient(listOf(Primary, PrimaryDim)),
                                RoundedCornerShape(9999.dp)
                            )
                            .clickable { onEnter() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = OnPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ═══ Overlays ═══
            when (keyboardMode) {
            KeyboardViewModel.KeyboardMode.TONE_SELECT -> {
                ToneSelectorSheet(
                    currentTone = currentTone,
                    onToneSelected = { viewModel.selectTone(it) },
                    onCustomPromptChanged = { viewModel.updateCustomPrompt(it) },
                    customPrompt = viewModel.customPrompt.collectAsState().value,
                    onApply = { viewModel.dismissOverlay() },
                    onDismiss = { viewModel.dismissOverlay() }
                )
            }
            KeyboardViewModel.KeyboardMode.REWRITE_RESULTS -> {
                RewriteResultsSheet(
                    rewriteState = rewriteState,
                    currentTone = currentTone,
                    onCopy = { /* Copy to clipboard */ },
                    onInsert = { text -> onInsertRewrite(text) },
                    onRegenerate = { viewModel.regenerate() },
                    onAdjustTone = { viewModel.showToneSelector() },
                    onDismiss = { viewModel.dismissOverlay() }
                )
            }
            KeyboardViewModel.KeyboardMode.EMOJI_STRIP -> {
                EmojiStrip(
                    emojis = (rewriteState as? RewriteState.Success)?.result?.emojis ?: emptyList(),
                    toneName = currentTone.displayName,
                    toneEmoji = currentTone.emoji,
                    onEmojiClick = { emoji -> onKeyPress(emoji) },
                    onDismiss = { viewModel.dismissOverlay() }
                )
            }
            else -> { /* Typing mode — no overlay */ }
        }
        }
    }
}

/**
 * Toolbar button matching HTML: bg-surface-container-highest, rounded-full,
 * text-[13px] font-semibold
 */
@Composable
private fun RowScope.ToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                SurfaceContainerHighest,
                RoundedCornerShape(9999.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isPrimary) Primary else OnSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = if (isPrimary) Primary else OnSurfaceVariant
            )
        }
    }
}
