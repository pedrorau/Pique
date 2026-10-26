package com.pedrorau.pique.knots

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** Doubled strands without an explicit spread use this gap (two lines just apart). */
private const val DEFAULT_SPREAD = 2.2f
private const val CURVE_SAMPLES_PER_SEGMENT = 16
private const val CAP_POINTS = 14

/**
 * A strand resampled to a fixed number of evenly spaced points, stored as `x, y, z, spread`
 * per point. Same-sized samples of one strand in two steps can be blended point by point.
 */
class StrandSample(val values: FloatArray) {
    val size get() = values.size / 4
    fun x(i: Int) = values[i * 4]
    fun y(i: Int) = values[i * 4 + 1]
    fun z(i: Int) = values[i * 4 + 2]
    fun spread(i: Int) = values[i * 4 + 3]
}

/** A drawable polyline: `x, y, z` per point plus, per point, its fraction along the strand. */
class Polyline(val xyz: FloatArray, val fraction: FloatArray) {
    val size get() = fraction.size
}

/** Every step of a knot, each strand resampled once up front. */
class KnotFrames(val knot: Knot) {
    val steps: List<List<StrandSample>> = knot.steps.map { step ->
        step.strands.mapIndexed { k, points -> sampleStrand(points, knot.strands[k].samples) }
    }

    /** Strands blended between two steps; [t] 0..1. */
    fun blend(from: Int, to: Int, t: Float): List<StrandSample> =
        steps[from].indices.map { k ->
            val a = steps[from][k].values
            val b = steps[to][k].values
            StrandSample(FloatArray(a.size) { i -> a[i] + (b[i] - a[i]) * t })
        }
}

/** Smooth curve through the control points, resampled to [count] evenly spaced points. */
fun sampleStrand(points: List<List<Float>>, count: Int): StrandSample {
    val control = points.map { p ->
        floatArrayOf(p[0], p[1], p.getOrElse(2) { 0.5f }, p.getOrElse(3) { DEFAULT_SPREAD })
    }
    return StrandSample(resample(catmullRom(control), count))
}

// Uniform Catmull-Rom through every control point; the ends are extended by reflection.
private fun catmullRom(control: List<FloatArray>): List<FloatArray> {
    if (control.size < 2) return control
    val p = buildList {
        add(FloatArray(4) { 2 * control[0][it] - control[1][it] })
        addAll(control)
        add(FloatArray(4) { 2 * control.last()[it] - control[control.size - 2][it] })
    }
    val out = ArrayList<FloatArray>()
    for (i in 1 until p.size - 2) {
        val p0 = p[i - 1]
        val p1 = p[i]
        val p2 = p[i + 1]
        val p3 = p[i + 2]
        for (s in 0 until CURVE_SAMPLES_PER_SEGMENT) {
            val t = s.toFloat() / CURVE_SAMPLES_PER_SEGMENT
            val t2 = t * t
            val t3 = t2 * t
            out.add(FloatArray(4) { c ->
                0.5f * (2 * p1[c] + (-p0[c] + p2[c]) * t +
                    (2 * p0[c] - 5 * p1[c] + 4 * p2[c] - p3[c]) * t2 +
                    (-p0[c] + 3 * p1[c] - 3 * p2[c] + p3[c]) * t3)
            })
        }
    }
    out.add(p[p.size - 2])
    return out
}

private fun resample(curve: List<FloatArray>, count: Int): FloatArray {
    val lengths = FloatArray(curve.size)
    for (i in 1 until curve.size) {
        lengths[i] = lengths[i - 1] + hypot(curve[i][0] - curve[i - 1][0], curve[i][1] - curve[i - 1][1])
    }
    val total = lengths.last()
    val out = FloatArray(count * 4)
    var j = 0
    for (n in 0 until count) {
        val target = if (count == 1) 0f else total * n / (count - 1)
        while (j < curve.size - 2 && lengths[j + 1] < target) j++
        val span = lengths[j + 1] - lengths[j]
        val f = if (span > 0f) ((target - lengths[j]) / span).coerceIn(0f, 1f) else 0f
        for (c in 0 until 4) out[n * 4 + c] = curve[j][c] + (curve[j + 1][c] - curve[j][c]) * f
    }
    return out
}

/** What gets drawn for a strand: one line, or for a bight both lines and the turn at its end. */
fun StrandSample.toPolyline(kind: StrandKind): Polyline {
    val n = size
    if (kind == StrandKind.SINGLE) {
        return Polyline(
            xyz = FloatArray(n * 3) { i -> values[(i / 3) * 4 + i % 3] },
            fraction = FloatArray(n) { it.toFloat() / (n - 1) }
        )
    }
    val total = n * 2 + CAP_POINTS
    val xyz = FloatArray(total * 3)
    val fraction = FloatArray(total)
    // Normal at each point from the local direction.
    val nx = FloatArray(n)
    val ny = FloatArray(n)
    for (i in 0 until n) {
        val a = maxOf(0, i - 1)
        val b = minOf(n - 1, i + 1)
        val dx = x(b) - x(a)
        val dy = y(b) - y(a)
        val len = hypot(dx, dy).takeIf { it > 1e-6f } ?: 1f
        nx[i] = -dy / len
        ny[i] = dx / len
    }
    fun put(index: Int, x: Float, y: Float, z: Float, f: Float) {
        xyz[index * 3] = x
        xyz[index * 3 + 1] = y
        xyz[index * 3 + 2] = z
        fraction[index] = f
    }
    for (i in 0 until n) {
        val half = spread(i) / 2f
        put(i, x(i) + nx[i] * half, y(i) + ny[i] * half, z(i), i.toFloat() / (n - 1))
        val back = n + CAP_POINTS + (n - 1 - i)
        put(back, x(i) - nx[i] * half, y(i) - ny[i] * half, z(i), i.toFloat() / (n - 1))
    }
    val last = n - 1
    val radius = spread(last) / 2f
    val start = atan2(ny[last], nx[last])
    for (c in 0 until CAP_POINTS) {
        val angle = start - PI.toFloat() * (c + 1) / (CAP_POINTS + 1)
        put(n + c, x(last) + radius * cos(angle), y(last) + radius * sin(angle), z(last), 1f)
    }
    return Polyline(xyz, fraction)
}
