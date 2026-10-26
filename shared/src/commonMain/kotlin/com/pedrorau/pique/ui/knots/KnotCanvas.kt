package com.pedrorau.pique.ui.knots

import com.pedrorau.pique.knots.Fixture
import com.pedrorau.pique.knots.FixtureKind
import com.pedrorau.pique.knots.Knot
import com.pedrorau.pique.knots.KnotFrames
import com.pedrorau.pique.knots.StrandSample
import com.pedrorau.pique.knots.toPolyline
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.material3.MaterialTheme

const val KNOT_BOX_WIDTH = 100f
const val KNOT_BOX_HEIGHT = 70f

private const val LINE_WIDTH = 1.3f
private const val HALO_WIDTH = 2.9f
private const val FIXTURE_WIDTH = 1.25f
/** Points per drawn piece; short pieces let crossings sort correctly by depth. */
private const val CHUNK = 3

/**
 * Draws a knot between two steps: [from] morphing into [to] at [progress] (0..1). The highlight
 * of [to] is drawn in the accent color. Crossings are drawn back to front, each piece with a halo
 * in the background color so the line underneath shows a gap.
 */
@Composable
fun KnotCanvas(
    frames: KnotFrames,
    from: Int,
    to: Int,
    progress: () -> Float,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    ink: Color = MaterialTheme.colorScheme.onSurface,
    accent: Color = MaterialTheme.colorScheme.tertiary,
    fixtureColor: Color = MaterialTheme.colorScheme.outline
) {
    Canvas(modifier = modifier) {
        val k = size.width / KNOT_BOX_WIDTH
        scale(k, k, pivot = Offset.Zero) {
            frames.knot.fixture?.let { drawFixture(it, fixtureColor) }
            val strands = frames.blend(from, to, progress())
            val highlight = frames.knot.steps[to].highlight
            drawStrands(frames.knot, strands, highlight, background, ink, accent)
        }
    }
}

private class Piece(val z: Float, val path: Path, val highlighted: Boolean)

private fun DrawScope.drawStrands(
    knot: Knot,
    strands: List<StrandSample>,
    highlight: Map<String, List<Float>>,
    background: Color,
    ink: Color,
    accent: Color
) {
    val pieces = ArrayList<Piece>()
    strands.forEachIndexed { index, strand ->
        val line = strand.toPolyline(knot.strands[index].kind)
        val range = highlight[index.toString()]
        var i = 0
        while (i < line.size - 1) {
            val end = minOf(i + CHUNK, line.size - 1)
            val path = Path()
            var zSum = 0f
            for (p in i..end) {
                val x = line.xyz[p * 3]
                val y = line.xyz[p * 3 + 1]
                if (p == i) path.moveTo(x, y) else path.lineTo(x, y)
                zSum += line.xyz[p * 3 + 2]
            }
            val f = line.fraction[(i + end) / 2]
            val lit = range != null && f >= range[0] && f <= range[1]
            pieces.add(Piece(zSum / (end - i + 1), path, lit))
            i = end
        }
    }
    pieces.sortBy { it.z }
    val halo = Stroke(width = HALO_WIDTH, cap = StrokeCap.Butt, join = StrokeJoin.Round)
    val stroke = Stroke(width = LINE_WIDTH, cap = StrokeCap.Round, join = StrokeJoin.Round)
    for (piece in pieces) {
        drawPath(piece.path, background, style = halo)
        drawPath(piece.path, if (piece.highlighted) accent else ink, style = stroke)
    }
}

private fun DrawScope.drawFixture(fixture: Fixture, color: Color) {
    val stroke = Stroke(width = FIXTURE_WIDTH, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val x = fixture.x
    val y = fixture.y
    when (fixture.kind) {
        FixtureKind.HOOK -> {
            // Eye, shank, bend and point with barb.
            drawCircle(color, radius = 2.3f, center = Offset(x, y), style = stroke)
            val hook = Path().apply {
                moveTo(x, y + 2.3f)
                lineTo(x, y + 30f)
                cubicTo(x, y + 39f, x + 10f, y + 39f, x + 10f, y + 31f)
                lineTo(x + 10f, y + 24f)
                lineTo(x + 8f, y + 27f)
            }
            drawPath(hook, color, style = stroke)
        }
        FixtureKind.RING -> drawCircle(color, radius = 3.2f, center = Offset(x, y), style = stroke)
    }
}
