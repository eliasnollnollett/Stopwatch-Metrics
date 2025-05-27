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
        primary      = Color.Black,
        onPrimary    = Color.White,
        background   = Color.White,
        onBackground = onBg,
        surface      = onBg.lighten(0.7f),
        onSurface    = Color.White,
        // …other slots…
    )
}

private val DarkColors = run {
    val onBg = Color.White
    darkColorScheme(
        primary      = Color.White,
        onPrimary    = Color.Black,
        background   = Color.Black,
        onBackground = onBg,
        surface      = onBg.darken(0.7f),
        onSurface    = Color.White,
        // …other slots…
    )
}

@Composable
fun MyApplicationTheme(
    useDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography  = Typography(),
        content     = content
    )
}
