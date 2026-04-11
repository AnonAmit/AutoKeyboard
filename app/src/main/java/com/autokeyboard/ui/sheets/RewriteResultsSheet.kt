package com.autokeyboard.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autokeyboard.data.model.RewriteResult
import com.autokeyboard.data.model.RewriteState
import com.autokeyboard.data.model.Tone
import com.autokeyboard.ui.components.ShimmerResultSkeleton
import com.autokeyboard.ui.theme.*

/**
 * Rewrite Results Bottom Sheet — pixel-matched to ai_rewrite_results/code.html.
 * Shows AI suggestions with copy/insert buttons, emoji strip, shimmer loading.
 */
@Composable
fun RewriteResultsSheet(
    rewriteState: RewriteState,
    currentTone: Tone,
    onCopy: (String) -> Unit,
    onInsert: (String) -> Unit,
    onRegenerate: () -> Unit,
    onAdjustTone: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .background(
                SheetBackground,
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
    ) {
        // Drag handle
        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .background(OutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(9999.dp))
            )
        }

        // Header: "✨ AI Suggestions" + tone badge + close
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "✨ AI Suggestions",
                    style = MaterialTheme.typography.titleLarge,
                    color = OnSurface
                )
                // Tone badge
                Box(
                    modifier = Modifier
                        .background(
                            Primary.copy(alpha = 0.2f),
                            RoundedCornerShape(9999.dp)
                        )
                        .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(9999.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = currentTone.displayName.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = PrimaryFixedDim,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Close button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        SurfaceContainerHighest.copy(alpha = 0.5f),
                        RoundedCornerShape(9999.dp)
                    )
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Close, "Close", tint = OnSurfaceVariant)
            }
        }

        Divider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            when (rewriteState) {
                is RewriteState.Loading -> {
                    ShimmerResultSkeleton()
                    ShimmerResultSkeleton()
                    ShimmerResultSkeleton()
                }
                is RewriteState.Success -> {
                    rewriteState.result.variants.forEach { variant ->
                        SuggestionCard(
                            text = variant,
                            emojis = rewriteState.result.emojis,
                            onCopy = { onCopy(variant) },
                            onInsert = { onInsert(variant) }
                        )
                    }
                }
                is RewriteState.Streaming -> {
                    SuggestionCard(
                        text = rewriteState.partialText,
                        emojis = emptyList(),
                        onCopy = { onCopy(rewriteState.partialText) },
                        onInsert = { onInsert(rewriteState.partialText) }
                    )
                    ShimmerResultSkeleton()
                }
                is RewriteState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ErrorContainer.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = rewriteState.message,
                            color = OnErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                else -> {}
            }
        }

        // Bottom CTA: Regenerate + Adjust Tone
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceContainerHigh)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Regenerate (filled)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .background(
                        Color(0xFF7C4DFF),
                        RoundedCornerShape(9999.dp)
                    )
                    .clickable { onRegenerate() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Refresh, "Regenerate", tint = Color.White, modifier = Modifier.size(20.dp))
                    Text("Regenerate", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color.White)
                }
            }

            // Adjust Tone (outlined)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(9999.dp))
                    .clickable { onAdjustTone() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Tune, "Tune", tint = Primary, modifier = Modifier.size(20.dp))
                    Text("Adjust Tone", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Primary)
                }
            }
        }
    }
}

/**
 * Individual suggestion card — bg key-tiles, rounded-2xl, copy + insert buttons.
 */
@Composable
private fun SuggestionCard(
    text: String,
    emojis: List<String>,
    onCopy: () -> Unit,
    onInsert: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KeyTiles, RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Copy button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceContainerHighest, RoundedCornerShape(12.dp))
                        .clickable { onCopy() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ContentCopy, "Copy", tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                // Insert button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PrimaryContainer, RoundedCornerShape(12.dp))
                        .clickable { onInsert() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Input, "Insert", tint = OnPrimaryContainer, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Emoji strip (if available)
        if (emojis.isNotEmpty()) {
            Divider(color = Color.White.copy(alpha = 0.05f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Context:", fontSize = 12.sp, color = OnSurfaceVariant)
                Row(
                    modifier = Modifier
                        .background(Background.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    emojis.take(5).forEach { emoji ->
                        Text(emoji, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
