package com.pedrorau.pique.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import com.pedrorau.pique.ui.theme.DarkPiqueColors
import com.pedrorau.pique.ui.theme.PiqueTheme
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Still frame of the rip-current beach seen from above: two sandbars with foam, the dark channel
 * between them, the curved shore and the casting target at the channel edge.
 */
@Composable
fun BeachThumbnail(modifier: Modifier = Modifier) {
    val colors = PiqueTheme.colors
    // The water is always dark, so the target uses the light-on-dark accent in both themes.
    val target = DarkPiqueColors.accent
    // Foam positions as fractions of the canvas, seeded so the drawing never jumps between frames.
    val foam = remember {
        val random = Random(7)
        List(70) {
            val leftBar = random.nextBoolean()
            val x = if (leftBar) random.nextFloat() * 0.42f else 0.58f + random.nextFloat() * 0.42f
            val y = 0.5f + (random.nextFloat() + random.nextFloat() - 1f) * 0.06f
            Triple(x, y, 0.8f + random.nextFloat() * 1.6f)
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRect(
            brush = Brush.verticalGradient(
                0f to colors.seaDeep,
                0.7f to Color(0xFF1D6F80),
                1f to colors.seaShallow
            )
        )

        // Sandbars: soft ellipses (a radial gradient squashed vertically).
        val barCenterY = h * 0.5f
        for (cx in listOf(w * 0.18f, w * 0.82f)) {
            scale(scaleX = 1f, scaleY = 0.18f, pivot = Offset(cx, barCenterY)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.seaShallow, colors.seaShallow.copy(alpha = 0f)),
                        center = Offset(cx, barCenterY),
                        radius = w * 0.34f
                    ),
                    radius = w * 0.34f,
                    center = Offset(cx, barCenterY)
                )
            }
        }

        // Swell crests bending toward the channel.
        for ((i, y) in listOf(0.18f, 0.34f).withIndex()) {
            val dip = h * (0.05f + 0.04f * i)
            val crest = Path().apply {
                moveTo(0f, h * y)
                cubicTo(w * 0.33f, h * y, w * 0.42f, h * y + dip * 0.6f, w * 0.5f, h * y + dip)
                cubicTo(w * 0.58f, h * y + dip * 0.6f, w * 0.67f, h * y, w, h * y)
            }
            drawPath(crest, colors.foam.copy(alpha = 0.4f), style = Stroke(width = 1.5f))
        }

        // Waves breaking on the bars, never in the channel.
        val breakWidth = 4f
        drawLine(colors.foam, Offset(0f, barCenterY - 4f), Offset(w * 0.42f, barCenterY - 2f), breakWidth, StrokeCap.Round)
        drawLine(colors.foam, Offset(w * 0.58f, barCenterY - 2f), Offset(w, barCenterY - 4f), breakWidth, StrokeCap.Round)
        for ((fx, fy, r) in foam) {
            drawCircle(colors.foam.copy(alpha = 0.7f), radius = r, center = Offset(fx * w, fy * h))
        }

        // Shore with a small embayment behind the channel.
        val shoreAt = { x: Float ->
            h * 0.86f + h * 0.07f * exp(-((x / w - 0.5f) / 0.16f).let { it * it }) + sin(x / 14f)
        }
        val sand = Path().apply {
            moveTo(0f, shoreAt(0f))
            var x = 0f
            while (x <= w) {
                lineTo(x, shoreAt(x))
                x += 6f
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(sand, colors.sand)

        // Casting target at the channel edge.
        val targetCenter = Offset(w * 0.56f, h * 0.6f)
        drawCircle(target, radius = 6f * density, center = targetCenter, style = Stroke(width = 2.5f * density))
        drawCircle(target, radius = 2.5f * density, center = targetCenter)
    }
}
