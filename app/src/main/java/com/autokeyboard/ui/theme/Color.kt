package com.autokeyboard.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════
// Neon Nocturne — "The Obsidian Architect" Design System Colors
// Extracted from HTML UI screens (ground truth)
// ═══════════════════════════════════════════════════════════════

// Surface hierarchy (OLED-safe deep-space palette)
val Background = Color(0xFF0C0C1F)
val Surface = Color(0xFF0C0C1F)
val SurfaceDim = Color(0xFF0C0C1F)
val SurfaceContainerLowest = Color(0xFF000000)
val SurfaceContainerLow = Color(0xFF111127)
val SurfaceContainer = Color(0xFF17172F)
val SurfaceContainerHigh = Color(0xFF1D1D37)
val SurfaceContainerHighest = Color(0xFF23233F)
val SurfaceBright = Color(0xFF292948)
val SurfaceVariant = Color(0xFF23233F)

// Primary (purple accent)
val Primary = Color(0xFFB6A0FF)
val PrimaryDim = Color(0xFF7E51FF)
val PrimaryContainer = Color(0xFFA98FFF)
val PrimaryFixedDim = Color(0xFF9C7EFF)
val PrimaryFixed = Color(0xFFA98FFF)
val OnPrimary = Color(0xFF340090)
val OnPrimaryContainer = Color(0xFF280072)
val OnPrimaryFixed = Color(0xFF000000)
val OnPrimaryFixedVariant = Color(0xFF32008A)
val InversePrimary = Color(0xFF6834EB)

// Secondary (teal accent)
val Secondary = Color(0xFF4AF8E3)
val SecondaryDim = Color(0xFF33E9D5)
val SecondaryContainer = Color(0xFF006A60)
val SecondaryFixed = Color(0xFF4AF8E3)
val SecondaryFixedDim = Color(0xFF33E9D5)
val OnSecondary = Color(0xFF005B51)
val OnSecondaryContainer = Color(0xFFDCFFF8)
val OnSecondaryFixed = Color(0xFF00463F)
val OnSecondaryFixedVariant = Color(0xFF00655B)

// Tertiary (lavender)
val Tertiary = Color(0xFFF2EFFF)
val TertiaryDim = Color(0xFFD4D1F0)
val TertiaryContainer = Color(0xFFE2DFFE)
val TertiaryFixed = Color(0xFFE2DFFE)
val TertiaryFixedDim = Color(0xFFD4D1F0)
val OnTertiary = Color(0xFF585871)
val OnTertiaryContainer = Color(0xFF504F68)
val OnTertiaryFixed = Color(0xFF3D3D55)
val OnTertiaryFixedVariant = Color(0xFF5A5972)

// Text / On-Surface
val OnBackground = Color(0xFFE5E3FF)
val OnSurface = Color(0xFFE5E3FF)
val OnSurfaceVariant = Color(0xFFAAA8C3)

// Outline
val Outline = Color(0xFF74738B)
val OutlineVariant = Color(0xFF46465C)

// Error
val Error = Color(0xFFFF6E84)
val ErrorDim = Color(0xFFD73357)
val ErrorContainer = Color(0xFFA70138)
val OnError = Color(0xFF490013)
val OnErrorContainer = Color(0xFFFFB2B9)

// Inverse
val InverseSurface = Color(0xFFFCF8FF)
val InverseOnSurface = Color(0xFF53536A)

// Surface tint
val SurfaceTint = Color(0xFFB6A0FF)

// ═══════════════════════════════════════════════════════════════
// Custom tokens (not in Material3 spec)
// ═══════════════════════════════════════════════════════════════
val KeyTiles = Color(0xFF2D2D44)
val SheetBackground = Color(0xFF1A1A2E)

// Gradient endpoints for CTA buttons
val GradientStart = Primary    // #B6A0FF
val GradientEnd = PrimaryDim   // #7E51FF

// Shimmer colors
val ShimmerBase = SurfaceContainerHighest  // #23233F
val ShimmerHighlight = KeyTiles             // #2D2D44
