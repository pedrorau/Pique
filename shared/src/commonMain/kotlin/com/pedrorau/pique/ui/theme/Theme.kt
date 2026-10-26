package com.pedrorau.pique.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

private val PiqueShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val LocalPiqueColors = staticCompositionLocalOf { LightPiqueColors }
private val LocalPiqueTypography = staticCompositionLocalOf<PiqueTypography> {
    error("PiqueTypography not provided; wrap content in PiqueTheme")
}

// Tokens Material 3 has no slot for: illustration and status colors, the data text style.
object PiqueTheme {
    val colors: PiqueColors
        @Composable @ReadOnlyComposable get() = LocalPiqueColors.current
    val typography: PiqueTypography
        @Composable @ReadOnlyComposable get() = LocalPiqueTypography.current
}

@Composable
fun PiqueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkPiqueColors else LightPiqueColors
    CompositionLocalProvider(
        LocalPiqueColors provides colors,
        LocalPiqueTypography provides piqueExtraTypography(),
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(darkTheme),
            typography = piqueTypography(),
            shapes = PiqueShapes,
            content = content
        )
    }
}
