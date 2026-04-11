package com.autokeyboard.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autokeyboard.ui.theme.*

/**
 * Emoji suggestion strip — pixel-matched to emoji_suggestion_strip/code.html.
 * bg-[#1A1A2E], 56dp height, tone badge, scrollable emoji row, dismiss X.
 */
@Composable
fun EmojiStrip(
    emojis: List<String>,
    toneName: String,
    toneEmoji: String,
    onEmojiClick: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayEmojis = emojis.ifEmpty {
        listOf("😊", "✨", "❤️", "😏", "🌹", "🥂", "🔥", "😜", "🍭", "💘")
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SheetBackground)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tone badge
        Row(
            modifier = Modifier
                .background(
                    PrimaryContainer.copy(alpha = 0.2f),
                    RoundedCornerShape(9999.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Tone: $toneName $toneEmoji",
                fontSize = 10.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Primary,
                letterSpacing = (-0.5).sp
            )
        }

        // Scrollable emoji row
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            displayEmojis.forEach { emoji ->
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 32.dp)
                        .background(KeyTiles, RoundedCornerShape(9999.dp))
                        .clickable { onEmojiClick(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 20.sp)
                }
            }
        }

        // Dismiss X
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Close",
            tint = OnSurfaceVariant,
            modifier = Modifier
                .size(20.dp)
                .clickable { onDismiss() }
        )
    }
}
