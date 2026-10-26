package com.pedrorau.pique.ui.readwater

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas as BitmapCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotateRad
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.pedrorau.pique.ui.readwater.RipSimulation.Companion.CX
import com.pedrorau.pique.ui.readwater.RipSimulation.Companion.GRID_H
import com.pedrorau.pique.ui.readwater.RipSimulation.Companion.GRID_W
import com.pedrorau.pique.ui.readwater.RipSimulation.Companion.H
import com.pedrorau.pique.ui.readwater.RipSimulation.Companion.TARGET_X
import com.pedrorau.pique.ui.readwater.RipSimulation.Companion.TARGET_Y
import com.pedrorau.pique.ui.readwater.RipSimulation.Companion.W
import com.pedrorau.pique.ui.theme.DarkPiqueColors
import com.pedrorau.pique.ui.theme.LightPiqueColors
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/** Markers keyed by legend number, in world units. 7 is the casting target, drawn next to it. */
private val MARKS = mapOf(
    1 to Offset(255f, 395f),
    2 to Offset(462f, 478f),
    3 to Offset(370f, 548f),
    4 to Offset(CX, 390f),
    5 to Offset(CX, 245f),
    6 to Offset(398f, 328f)
)
const val TARGET_MARK = 7

// The scene is an illustration with its own fixed palette in both themes (water is always dark).
private val Sand = LightPiqueColors.sand
private val WetSand = Color(0xFFBFA57C)
private val Foam = LightPiqueColors.foam
private val TracerColor = Color(0xFFFFD496)
private val FishColor = Color(0xFF061820)
private val MarkFill = Color(0xFF0E222A)
private val TargetColor = DarkPiqueColors.accent
private val DepthStops = listOf(
    0f to Color(0xFF84D2C0),
    0.32f to Color(0xFF30949A),
    0.68f to Color(0xFF145872),
    1f to Color(0xFF092E44)
)

private fun seaColor(depth: Float): Color {
    val t = min(1f, (depth.coerceAtLeast(0f) / 5.5f).pow(0.55f))
    for (k in 1 until DepthStops.size) {
        val (t1, c1) = DepthStops[k]
        if (t <= t1) {
            val (t0, c0) = DepthStops[k - 1]
            return lerp(c0, c1, (t - t0) / (t1 - t0))
        }
    }
    return DepthStops.last().second
}

/** Caches the painted depth map; repainted only when the tide moves. */
private class DepthLayer {
    val bitmap = ImageBitmap(GRID_W, GRID_H)
    private val canvas = BitmapCanvas(bitmap)
    private val paint = Paint()
    private val noise = Random(3).let { r -> FloatArray(GRID_W * GRID_H) { r.nextFloat() * 0.05f - 0.025f } }
    var paintedVersion = -1

    /** Smooth shoreline (sand below), traced from the exact depth so its edge isn't grid-stepped. */
    val shore = Path()

    fun repaint(sim: RipSimulation) {
        shore.reset()
        var x = 0f
        while (x <= W) {
            // Walk up from the bottom edge until the water starts.
            var y = H
            while (y > 400f && RipSimulation.depthAtMeanLevel(x, y) + sim.tide <= 0f) y -= 1f
            if (x == 0f) shore.moveTo(x, y) else shore.lineTo(x, y)
            x += 5f
        }
        shore.lineTo(W, H)
        shore.lineTo(0f, H)
        shore.close()

        for (k in sim.staticDepth.indices) {
            val d = sim.staticDepth[k] + sim.tide
            val n = noise[k]
            paint.color = when {
                d <= -0.15f -> Sand.copy(red = Sand.red + n, green = Sand.green + n, blue = Sand.blue + n)
                d <= 0f -> WetSand
                else -> seaColor(d)
            }
            val i = (k % GRID_W).toFloat()
            val j = (k / GRID_W).toFloat()
            canvas.drawRect(i, j, i + 1f, j + 1f, paint)
        }
        paintedVersion = sim.depthVersion
    }
}

/**
 * Draws [sim] scaled to the canvas. [frameNanos] is read inside the draw phase only, so each frame
 * redraws the canvas without recomposing the screen; [highlight] pulses one legend marker (0 = none).
 */
@Composable
fun RipCurrentCanvas(
    sim: RipSimulation,
    frameNanos: () -> Long,
    showFlow: Boolean,
    showFish: Boolean,
    showMarks: Boolean,
    highlight: Int,
    modifier: Modifier = Modifier
) {
    val depthLayer = remember { DepthLayer() }
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val time = frameNanos() / 1_000_000_000.0
        if (depthLayer.paintedVersion != sim.depthVersion) depthLayer.repaint(sim)

        drawImage(
            image = depthLayer.bitmap,
            dstSize = IntSize(size.width.toInt(), size.height.toInt()),
            dstOffset = IntOffset.Zero,
            filterQuality = FilterQuality.Low
        )

        val k = size.width / W
        scale(k, k, pivot = Offset.Zero) {
            drawPath(depthLayer.shore, WetSand, style = Stroke(width = 8f))
            drawPath(depthLayer.shore, Sand)
            if (showFish) drawFish(sim)
            if (showFlow) drawTracers(sim)
            drawFoam(sim)
            drawWaves(sim)
            drawAngler(sim)
        }

        val pulse = (0.5 + 0.5 * sin(time * 1000.0 / 260.0)).toFloat()
        drawTarget(k, pulse)
        if (showMarks) drawMarks(textMeasurer, k, highlight, pulse)
        drawScaleBar(textMeasurer, k)
    }
}

private fun DrawScope.drawFish(sim: RipSimulation) {
    for (f in sim.fish) {
        translate(f.x, f.y) {
            rotateRad(f.angle, pivot = Offset.Zero) {
                val len = f.length
                drawOval(FishColor.copy(alpha = 0.78f), topLeft = Offset(-len / 2f, -len * 0.17f), size = Size(len, len * 0.34f))
                val wiggle = (sin(f.phase * 9f + f.index) * 0.45f)
                translate(-len / 2f + 1f, 0f) {
                    rotateRad(wiggle, pivot = Offset.Zero) {
                        val tail = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(-len * 0.32f, -len * 0.17f)
                            lineTo(-len * 0.32f, len * 0.17f)
                            close()
                        }
                        drawPath(tail, FishColor.copy(alpha = 0.78f))
                    }
                }
            }
        }
    }
}

// Trails batched into a few paths by brightness: one draw call per bucket instead of one per trail.
private fun DrawScope.drawTracers(sim: RipSimulation) {
    val buckets = Array(4) { Path() }
    for (t in sim.tracers) {
        if (t.historyCount < 3) continue
        val alpha = min(0.9f, 0.12f + sim.tracerSpeed(t) / 45f) * min(1f, t.life)
        if (alpha <= 0.05f) continue
        val path = buckets[(alpha / 0.9f * 3.999f).toInt().coerceIn(0, 3)]
        path.moveTo(t.history[0], t.history[1])
        for (i in 1 until t.historyCount) path.lineTo(t.history[i * 2], t.history[i * 2 + 1])
    }
    val stroke = Stroke(width = 1.6f, cap = StrokeCap.Round)
    buckets.forEachIndexed { b, path ->
        drawPath(path, TracerColor.copy(alpha = 0.9f * (b + 0.5f) / 4f), style = stroke)
    }
}

// Foam particles batched by size and fade into a few drawPoints calls.
private fun DrawScope.drawFoam(sim: RipSimulation) {
    val buckets = Array(12) { ArrayList<Offset>() }
    for (i in 0 until sim.foamCount) {
        val fade = (sim.foamLife[i] / sim.foamMaxLife[i]).coerceIn(0f, 0.999f)
        val sizeBucket = when {
            sim.foamRadius[i] < 1.6f -> 0
            sim.foamRadius[i] < 2.4f -> 1
            else -> 2
        }
        buckets[sizeBucket * 4 + (fade * 4).toInt()].add(Offset(sim.foamX[i], sim.foamY[i]))
    }
    val radii = floatArrayOf(1.2f, 2f, 3f)
    for (b in buckets.indices) {
        if (buckets[b].isEmpty()) continue
        val alpha = 0.6f * ((b % 4) + 0.5f) / 4f
        drawPoints(buckets[b], PointMode.Points, Foam.copy(alpha = alpha), strokeWidth = radii[b / 4] * 2f, cap = StrokeCap.Round)
    }
}

// Each wave becomes a handful of paths: its shadow, the unbroken crest and breaking crests by thickness.
private fun DrawScope.drawWaves(sim: RipSimulation) {
    val shadowColor = Color(0xFF041C28).copy(alpha = 0.22f)
    val crestColor = Color(0xFFD6F2EE)
    for (w in sim.waves) {
        val shadow = Path()
        val crest = Path()
        val breakers = Array(3) { Path() }
        var heightSum = 0f
        var crestSegments = 0
        for (i in 0 until w.size - 1) {
            if (w.done[i] || w.done[i + 1]) continue
            val breaking = (w.breaking[i] + w.breaking[i + 1]) / 2f
            val height = (w.heights[i] + w.heights[i + 1]) / 2f
            val x0 = w.x(i)
            val x1 = w.x(i + 1)
            if (breaking > 0.25f) {
                val path = breakers[(height / 0.8f).toInt().coerceIn(0, 2)]
                path.moveTo(x0, w.ys[i])
                path.lineTo(x1, w.ys[i + 1])
            } else {
                crest.moveTo(x0, w.ys[i])
                crest.lineTo(x1, w.ys[i + 1])
                shadow.moveTo(x0, w.ys[i] + 5f)
                shadow.lineTo(x1, w.ys[i + 1] + 5f)
                heightSum += height
                crestSegments++
            }
        }
        if (crestSegments > 0) {
            drawPath(shadow, shadowColor, style = Stroke(width = 4f, cap = StrokeCap.Round))
            val alpha = 0.2f + 0.22f * (heightSum / crestSegments)
            drawPath(crest, crestColor.copy(alpha = alpha), style = Stroke(width = 1.4f, cap = StrokeCap.Round))
        }
        breakers.forEachIndexed { b, path ->
            drawPath(path, Foam.copy(alpha = 0.92f), style = Stroke(width = 2.6f + b * 2.4f, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawAngler(sim: RipSimulation) {
    val x = RipSimulation.ANGLER_X
    val y = sim.anglerY()
    val tip = Offset(x - 24f, y - 26f)
    val line = Path().apply {
        moveTo(tip.x, tip.y)
        quadraticTo((tip.x + TARGET_X) / 2f + 10f, min(tip.y, TARGET_Y) - 40f, TARGET_X, TARGET_Y)
    }
    drawPath(
        line,
        Color.White.copy(alpha = 0.75f),
        style = Stroke(width = 0.9f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
    )
    drawOval(Color(0xFF3C2D19).copy(alpha = 0.25f), topLeft = Offset(x - 7f, y - 2f), size = Size(20f, 12f))
    drawLine(Color(0xFF1D2A30), Offset(x - 3f, y - 3f), tip, strokeWidth = 1.6f)
    drawOval(Color(0xFF2F4650), topLeft = Offset(x - 9f, y - 6f), size = Size(18f, 12f))
    drawCircle(LightPiqueColors.accent, radius = 4.2f, center = Offset(x - 1f, y - 1f))
}

private fun DrawScope.drawTarget(k: Float, pulse: Float) {
    val center = Offset(TARGET_X * k, TARGET_Y * k)
    drawCircle(TargetColor, radius = (5f + 4f * pulse) * density, center = center, style = Stroke(width = 2f * density))
    drawCircle(TargetColor, radius = 2.5f * density, center = center)
}

private fun DrawScope.drawMarks(textMeasurer: TextMeasurer, k: Float, highlight: Int, pulse: Float) {
    val r = 11f * density
    val style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold)
    val marks = MARKS.map { (n, p) -> n to Offset(p.x * k, p.y * k) } +
        (TARGET_MARK to Offset(TARGET_X * k + r * 1.8f, TARGET_Y * k - r * 1.8f))
    for ((n, center) in marks) {
        if (n == highlight) {
            drawCircle(
                Color.White.copy(alpha = 0.4f + 0.6f * pulse),
                radius = r + (5f + 4f * pulse) * density,
                center = center,
                style = Stroke(width = 2f * density)
            )
        }
        val isTarget = n == TARGET_MARK
        drawCircle(if (isTarget) TargetColor else MarkFill.copy(alpha = 0.88f), radius = r, center = center)
        if (!isTarget) drawCircle(Color.White, radius = r, center = center, style = Stroke(width = 1.3f * density))
        val layout = textMeasurer.measure(
            n.toString(),
            style.copy(color = if (isTarget) DarkPiqueColors.onAccent else Color.White)
        )
        drawText(
            layout,
            topLeft = Offset(center.x - layout.size.width / 2f, center.y - layout.size.height / 2f)
        )
    }
}

// "Mar adentro" label at the top and a scale bar on the sand: 100 units ≈ 50 m.
private fun DrawScope.drawScaleBar(textMeasurer: TextMeasurer, k: Float) {
    val labelStyle = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    drawText(
        textMeasurer.measure("↑ MAR ADENTRO", labelStyle.copy(color = Color.White.copy(alpha = 0.7f))),
        topLeft = Offset(12f * density, 8f * density)
    )
    val ink = Color(0xFF1E2828).copy(alpha = 0.75f)
    val x0 = 14f * density
    val y0 = H * k - 12f * density
    val x1 = x0 + 100f * k
    drawLine(ink, Offset(x0, y0), Offset(x1, y0), strokeWidth = 1.5f * density)
    drawLine(ink, Offset(x0, y0 - 4f * density), Offset(x0, y0 + density), strokeWidth = 1.5f * density)
    drawLine(ink, Offset(x1, y0 - 4f * density), Offset(x1, y0 + density), strokeWidth = 1.5f * density)
    val label = textMeasurer.measure("≈50 m", labelStyle.copy(color = ink, letterSpacing = 0.sp))
    drawText(label, topLeft = Offset(x1 + 6f * density, y0 - label.size.height / 2f))
}

