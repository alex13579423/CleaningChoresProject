package com.example.myapp.ui.theme

import androidx.compose.ui.graphics.Color

// Industry Standard Material 3 Dark Palette
val Primary = Color(0xFFFFFFFF)       // Rich Forest Green (Less washed out, but professional)
val OnPrimary = Color(0xFF4CAF50)
val PrimaryContainer = Color(0xFF185E20)
val OnPrimaryContainer = Color(0xFFA5D6A7)

val Secondary = Color(0xFF43A047)     // Slightly brighter green for secondary actions
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFF3D7C40).copy(alpha = 0.3f)
val OnSecondaryContainer = Color(0xFFC8E6C9)

val Tertiary = Color(0xFF81C784)
val OnTertiary = Color(0xFF003300)
val TertiaryContainer = Color(0xFF2E7D32)
val OnTertiaryContainer = Color(0xFFE8F5E9)

val Background = Color(0xFF0D0F0D)    // Slightly darker background
val OnBackground = Color(0xFFE8F5E9)
val Surface = Color(0xFF151915)       // Subtle green tint to the surface
val OnSurface = Color(0xFFE8F5E9)
val SurfaceVariant = Color(0xFF2C332C)
val OnSurfaceVariant = Color(0xFFB9CCB4)

val Outline = Color(0xFF727971)
val Error = Color(0xFFCF6679)
val OnError = Color(0xFF121212)

// Custom Colors for specific statuses
val PriorityHigh = Color(0xFFEF9A9A) // Clear Red
val PriorityMedium = Color(0xFFFFCC80) // Clear Orange
val PriorityLow = Color(0xFFA5D6A7) // Clear Green

val MaleColor = Color(0xFF90CAF9)
val FemaleColor = Color(0xFFF48FB1)

val UnavailableBackground = Color(0xFF3E2723) // Deep reddish-brown for absence
val UnavailableText = Color(0xFFFFAB91)       // Peachy pink for visibility
