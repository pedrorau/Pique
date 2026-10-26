package com.pedrorau.pique.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.pedrorau.pique.resources.Res
import com.pedrorau.pique.resources.archivo_variable
import com.pedrorau.pique.resources.atkinson_hyperlegible_bold
import com.pedrorau.pique.resources.atkinson_hyperlegible_regular
import com.pedrorau.pique.resources.ibm_plex_mono_medium
import org.jetbrains.compose.resources.Font

// Styles that Material 3 has no slot for.
@Immutable
data class PiqueTypography(
    val data: TextStyle,
)

@Composable
private fun displayFamily() = FontFamily(
    Font(Res.font.archivo_variable, FontWeight.Bold),
    Font(Res.font.archivo_variable, FontWeight.ExtraBold),
)

@Composable
private fun bodyFamily() = FontFamily(
    Font(Res.font.atkinson_hyperlegible_regular, FontWeight.Normal),
    Font(Res.font.atkinson_hyperlegible_bold, FontWeight.Bold),
)

@Composable
private fun monoFamily() = FontFamily(
    Font(Res.font.ibm_plex_mono_medium, FontWeight.Medium),
)

@Composable
fun piqueTypography(): Typography {
    val display = displayFamily()
    val body = bodyFamily()
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = display, fontWeight = FontWeight.ExtraBold),
        displayMedium = base.displayMedium.copy(fontFamily = display, fontWeight = FontWeight.ExtraBold),
        displaySmall = TextStyle(fontFamily = display, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.01).em),
        headlineLarge = base.headlineLarge.copy(fontFamily = display, fontWeight = FontWeight.Bold),
        headlineMedium = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
        headlineSmall = base.headlineSmall.copy(fontFamily = display, fontWeight = FontWeight.Bold),
        titleLarge = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
        titleMedium = base.titleMedium.copy(fontFamily = body, fontWeight = FontWeight.Bold),
        titleSmall = base.titleSmall.copy(fontFamily = body, fontWeight = FontWeight.Bold),
        bodyLarge = TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 26.sp),
        bodyMedium = TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
        bodySmall = base.bodySmall.copy(fontFamily = body),
        labelLarge = TextStyle(fontFamily = body, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.02.em),
        labelMedium = base.labelMedium.copy(fontFamily = body, fontWeight = FontWeight.Bold),
        labelSmall = TextStyle(fontFamily = body, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.1.em),
    )
}

@Composable
fun piqueExtraTypography() = PiqueTypography(
    data = TextStyle(fontFamily = monoFamily(), fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp),
)
