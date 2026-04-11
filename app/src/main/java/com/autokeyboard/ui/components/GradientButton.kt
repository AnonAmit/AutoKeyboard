package com.autokeyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.autokeyboard.ui.theme.*

/**
 * Primary CTA button with the signature gradient: primary -> primary-dim.
 * Matches the onboarding and tone selector "Apply" button from HTML designs.
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val gradient = Brush.linearGradient(
        colors = listOf(Primary, PrimaryDim)
    )

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(9999.dp))
            .background(if (enabled) gradient else Brush.linearGradient(
                listOf(Primary.copy(alpha = 0.3f), PrimaryDim.copy(alpha = 0.3f))
            ))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(9999.dp),
                ambientColor = Primary.copy(alpha = 0.1f),
                spotColor = PrimaryDim.copy(alpha = 0.2f)
            )
            .then(
                if (enabled) Modifier.clickable(onClick = onClick) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            if (icon != null) {
                icon()
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = OnPrimary
            )
        }
    }
}
