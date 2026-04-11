package com.autokeyboard.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AutoKeyboardShapes = Shapes(
    // Key radius: 4dp
    extraSmall = RoundedCornerShape(4.dp),
    // Card/input radius: 12dp
    small = RoundedCornerShape(12.dp),
    // Container radius: 16dp
    medium = RoundedCornerShape(16.dp),
    // Sheet radius: 24dp (top only handled per-component)
    large = RoundedCornerShape(24.dp),
    // Pill / full radius
    extraLarge = RoundedCornerShape(9999.dp)
)

// Common shape constants
val KeyShape = RoundedCornerShape(4.dp)
val CardShape = RoundedCornerShape(16.dp)
val SheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
val PillShape = RoundedCornerShape(9999.dp)
val InputShape = RoundedCornerShape(12.dp)
val ProviderCardShape = RoundedCornerShape(12.dp)
val OnboardingHeroShape = RoundedCornerShape(48.dp)
