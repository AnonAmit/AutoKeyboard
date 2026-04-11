package com.autokeyboard.ui.keyboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autokeyboard.ui.theme.*

/**
 * Individual keyboard key tile.
 * Matches HTML: bg-key-tiles, 42dp height, 4dp radius, shadow 0 1 2 rgba(0,0,0,0.4)
 * Active: bg-primary-container, text on-primary-container
 */
@Composable
fun KeyTile(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSpecial: Boolean = false,
    flex: Float = 1f,
    height: Dp = 42.dp,
    content: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor by animateColorAsState(
        targetValue = when {
            isPressed && !isSpecial -> PrimaryContainer
            isPressed && isSpecial -> Primary.copy(alpha = 0.2f)
            isSpecial -> SurfaceContainerHighest
            else -> KeyTiles
        },
        label = "keyBg"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isPressed && !isSpecial -> OnPrimaryContainer
            isSpecial -> OnSurfaceVariant
            else -> Color.White
        },
        label = "keyText"
    )

    Box(
        modifier = modifier
            .weight(flex)
            .height(height)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(4.dp),
                ambientColor = Color.Black.copy(alpha = 0.4f)
            )
            .background(bgColor, RoundedCornerShape(4.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (content != null) {
            content()
        } else {
            Text(
                text = label,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
        }
    }
}
