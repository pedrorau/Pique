package com.pedrorau.pique.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Values mirror docs/design-system.md; change both together.
@Immutable
data class PiqueColors(
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceSunken: Color,
    val ink: Color,
    val inkMuted: Color,
    val line: Color,
    val lineStrong: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val accent: Color,
    val onAccent: Color,
    val accentContainer: Color,
    val onAccentContainer: Color,
    // Illustration only (beach views, depth profiles); never for controls.
    val seaDeep: Color,
    val seaShallow: Color,
    val sand: Color,
    val foam: Color,
    // Status colors always go with an icon or text, never color alone.
    val ok: Color,
    val caution: Color,
    val danger: Color,
    val onDanger: Color,
)

val LightPiqueColors = PiqueColors(
    surface = Color(0xFFEEF3F2),
    surfaceRaised = Color(0xFFFFFFFF),
    surfaceSunken = Color(0xFFDFE8E7),
    ink = Color(0xFF0F2229),
    inkMuted = Color(0xFF4A6168),
    line = Color(0xFFC3D2D1),
    lineStrong = Color(0xFF6F878D),
    primary = Color(0xFF0B5563),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCBE9EC),
    onPrimaryContainer = Color(0xFF05333C),
    accent = Color(0xFFB8431A),
    onAccent = Color(0xFFFFFFFF),
    accentContainer = Color(0xFFFFDCCB),
    onAccentContainer = Color(0xFF4A1400),
    seaDeep = Color(0xFF0E3D55),
    seaShallow = Color(0xFF5FBFB3),
    sand = Color(0xFFD8C49C),
    foam = Color(0xFFF5FAF8),
    ok = Color(0xFF2B7A4B),
    caution = Color(0xFF8A5A00),
    danger = Color(0xFFB3261E),
    onDanger = Color(0xFFFFFFFF),
)

val DarkPiqueColors = PiqueColors(
    surface = Color(0xFF0B1619),
    surfaceRaised = Color(0xFF13242A),
    surfaceSunken = Color(0xFF07100F),
    ink = Color(0xFFE4EEEE),
    inkMuted = Color(0xFF96ADB2),
    line = Color(0xFF24404A),
    lineStrong = Color(0xFF6A868D),
    primary = Color(0xFF5CC0C9),
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF0F3D46),
    onPrimaryContainer = Color(0xFFCBE9EC),
    accent = Color(0xFFFF8A5C),
    onAccent = Color(0xFF3A1000),
    accentContainer = Color(0xFF5A2410),
    onAccentContainer = Color(0xFFFFDCCB),
    seaDeep = Color(0xFF082A3B),
    seaShallow = Color(0xFF3F9A90),
    sand = Color(0xFFB29A6E),
    foam = Color(0xFFE8F2EF),
    ok = Color(0xFF7FD6A2),
    caution = Color(0xFFF0C060),
    danger = Color(0xFFF2B8B5),
    onDanger = Color(0xFF601410),
)

fun PiqueColors.toColorScheme(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        tertiary = accent,
        onTertiary = onAccent,
        tertiaryContainer = accentContainer,
        onTertiaryContainer = onAccentContainer,
        background = surface,
        onBackground = ink,
        surface = surface,
        onSurface = ink,
        surfaceVariant = surfaceSunken,
        onSurfaceVariant = inkMuted,
        surfaceContainerLowest = surfaceRaised,
        surfaceContainerLow = surfaceRaised,
        surfaceContainer = surfaceRaised,
        surfaceContainerHigh = surfaceSunken,
        surfaceContainerHighest = surfaceSunken,
        outline = lineStrong,
        outlineVariant = line,
        error = danger,
        onError = onDanger,
    )
}
