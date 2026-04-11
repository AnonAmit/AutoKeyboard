package com.autokeyboard.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autokeyboard.ui.theme.*

/**
 * Onboarding screen — pixel-matched to onboarding_flow/code.html.
 * Full-screen hero with atmospheric glow, progress dots, "Next" CTA.
 */
@Composable
fun OnboardingScreen(
    onSkip: () -> Unit,
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 3

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Atmospheric glow blur (primary/10, blur 120dp)
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.Center)
                .offset(y = (-60).dp)
                .blur(120.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Primary.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        // Skip button
        Text(
            text = "Skip",
            color = OnSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .clickable { onSkip() }
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))

            // Hero card (256dp, 48dp corner radius)
            Box(
                modifier = Modifier
                    .size(256.dp)
                    .clip(RoundedCornerShape(48.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                SurfaceContainer,
                                SurfaceContainerHigh
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (currentStep) {
                    0 -> {
                        // Keyboard + AI icon
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Filled.Keyboard,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(64.dp)
                            )
                            Icon(
                                Icons.Filled.AutoFixHigh,
                                contentDescription = null,
                                tint = Secondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    1 -> {
                        // Privacy shield icon
                        Icon(
                            Icons.Filled.Security,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                    2 -> {
                        // Rewrite icon
                        Icon(
                            Icons.Filled.Psychology,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            // Headline with gradient word
            when (currentStep) {
                0 -> {
                    Text(
                        text = buildAnnotatedString {
                            append("Rewrite anything, ")
                            withStyle(SpanStyle(
                                brush = Brush.linearGradient(listOf(Primary, Secondary)),
                                fontWeight = FontWeight.ExtraBold
                            )) {
                                append("anywhere")
                            }
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        color = OnSurface,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "AutoKeyboard uses AI to rewrite your messages in any app with any tone you choose.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                1 -> {
                    Text(
                        text = buildAnnotatedString {
                            append("Your privacy, ")
                            withStyle(SpanStyle(
                                brush = Brush.linearGradient(listOf(Secondary, Primary)),
                                fontWeight = FontWeight.ExtraBold
                            )) {
                                append("guaranteed")
                            }
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        color = OnSurface
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No messages are stored. No data leaves your device except the single rewrite you trigger.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                2 -> {
                    Text(
                        text = buildAnnotatedString {
                            append("Choose your ")
                            withStyle(SpanStyle(
                                brush = Brush.linearGradient(listOf(Primary, PrimaryDim)),
                                fontWeight = FontWeight.ExtraBold
                            )) {
                                append("voice")
                            }
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        color = OnSurface
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Professional, Flirty, Gen-Z, Poetic — or write your own custom style instructions.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Progress dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                repeat(totalSteps) { step ->
                    Box(
                        modifier = Modifier
                            .size(if (step == currentStep) 32.dp else 8.dp, 8.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(
                                if (step == currentStep) Primary else OnSurfaceVariant.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Next / Get Started button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Brush.linearGradient(listOf(Primary, PrimaryDim)))
                    .clickable {
                        if (currentStep < totalSteps - 1) {
                            currentStep++
                        } else {
                            onComplete()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (currentStep < totalSteps - 1) "Next" else "Get Started",
                        style = MaterialTheme.typography.labelLarge,
                        color = OnPrimary,
                        fontSize = 16.sp
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = OnPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
