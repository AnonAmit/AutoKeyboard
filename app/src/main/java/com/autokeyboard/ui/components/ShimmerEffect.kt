package com.autokeyboard.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.autokeyboard.ui.theme.ShimmerBase
import com.autokeyboard.ui.theme.ShimmerHighlight

/**
 * Shimmer loading effect matching the HTML design:
 * linear-gradient(90deg, #23233f 25%, #2D2D44 50%, #23233f 75%)
 * background-size: 200% 100%, animation: 2s infinite linear
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    height: Dp = 24.dp,
    widthFraction: Float = 1f,
    cornerRadius: Dp = 9999.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
        start = Offset(translateAnim.value * 1000f, 0f),
        end = Offset((translateAnim.value + 1f) * 1000f, 0f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .background(brush, RoundedCornerShape(cornerRadius))
    )
}

/**
 * Shimmer loading skeleton for rewrite result cards.
 * Shows 3 shimmer bars at 75%, 100%, 50% width.
 */
@Composable
fun ShimmerResultSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                ShimmerBase.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerEffect(widthFraction = 0.75f, height = 16.dp)
        ShimmerEffect(widthFraction = 1f, height = 16.dp)
        ShimmerEffect(widthFraction = 0.5f, height = 16.dp)
    }
}
