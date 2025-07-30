package com.example.stopwatchmetrics.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.lerp


fun Color.lighten(fraction: Float) = lerp(this, Color.White, fraction)
fun Color.darken(fraction: Float)  = lerp(this, Color.Black, fraction)

private val LightColors = run {
    val onBg = Color.Black
    lightColorScheme(
        primary           = onBg,                    // Text/icons in most places
        onPrimary         = Color.White,
        secondary         = onBg.lighten(0.2f),      // subtle accent
        onSecondary       = Color.White,
        background        = Color.White,
        onBackground      = onBg,
        surface           = onBg.lighten(0.85f),      // your existing “surface”
        onSurface         = onBg,
        surfaceVariant    = onBg.lighten(0.85f),     // cards, buttons, etc.
        onSurfaceVariant  = onBg,
        outline           = onBg.lighten(0.5f)       // borders, dividers
    )
}

private val DarkColors = run {
    val onBg = Color.White
    darkColorScheme(
        primary           = onBg,
        onPrimary         = Color.Black,
        secondary         = onBg.darken(0.2f),
        onSecondary       = Color.Black,
        background        = Color.Black,
        onBackground      = onBg,
        surface           = onBg.darken(0.7f),
        onSurface         = Color.White,
        surfaceVariant    = onBg.darken(0.85f),
        onSurfaceVariant  = onBg,
        outline           = onBg.darken(0.5f)
    )
}

@Composable
fun MyApplicationTheme(
    useDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = Typography(), content = content)
}