package com.pedrorau.pique.ui.readwater

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh
import kotlin.random.Random

/**
 * Top-down model of a beach with a sandbar cut by a rip channel ("pozo").
 *
 * World is [W] × [H] units (1 unit ≈ 0.5 m), sea at the top, shore at the bottom. Waves travel at
 * shallow-water speed and break where height exceeds 0.78 × depth, so they break on the bars and
 * pass dark over the channel. The current is a pair of cells from a stream function that feed
 * along the shore and exit through the channel; low tide and bigger waves make it stronger.
 */
class RipSimulation(private val random: Random = Random(1)) {

    companion object {
        const val W = 1000f
        const val H = 680f
        const val CX = 500f

        // Depth grid used to paint the water; one cell = 5 world units.
        const val GRID_W = 200
        const val GRID_H = 136
        const val CELL = W / GRID_W

        private const val WAVE_SAMPLES = 101
        private const val SAMPLE_DX = W / (WAVE_SAMPLES - 1)
        private const val WAVE_PERIOD_S = 2.4f
        private const val BREAK_RATIO = 0.78f

        // Rip cell geometry: offshore edge (rip head), shoreward edge, jet half-width, cell half-span.
        private const val Y1 = 205f
        private const val Y2 = 600f
        private const val JET_WIDTH = 38f
        private const val CELL_SPAN = 470f
        private const val JET_SPEED = 45f

        private const val MAX_FOAM = 2200
        private const val TRACER_COUNT = 240
        const val TRACER_HISTORY = 18

        /** Where the angler casts: the edge of the channel, not its center. */
        const val TARGET_X = 556f
        const val TARGET_Y = 452f
        const val ANGLER_X = 652f

        fun shoreY(x: Float): Float =
            592f + 16f * exp(-sq((x - CX) / 110f)) + 4f * sin(x / 37f)

        /** Depth in meters at mean sea level; negative is dry sand. */
        fun depthAtMeanLevel(x: Float, y: Float): Float {
            val dist = shoreY(x) - y
            val dx = x - CX
            val barMask = 1f - exp(-sq(dx / 60f))
            return 0.012f * dist -
                1.75f * exp(-sq((dist - 190f) / 42f)) * barMask +
                0.9f * exp(-sq((dist - 95f) / 32f)) * (0.7f + 0.3f * (1f - barMask)) +
                1.1f * exp(-sq(dx / 52f)) * exp(-sq((dist - 150f) / 110f))
        }

        private fun sq(v: Float) = v * v
    }

    class Wave(height: Float) {
        val ys = FloatArray(WAVE_SAMPLES) { -12f }
        val heights = FloatArray(WAVE_SAMPLES) { height }
        /** 0..1 how much each sample is breaking (drives foam and the white crest). */
        val breaking = FloatArray(WAVE_SAMPLES)
        val done = BooleanArray(WAVE_SAMPLES)
        var alive = WAVE_SAMPLES
        fun x(i: Int) = i * SAMPLE_DX
        val size get() = WAVE_SAMPLES
    }

    class Tracer {
        var x = 0f
        var y = 0f
        var life = 0f
        val history = FloatArray(TRACER_HISTORY * 2)
        var historyCount = 0
    }

    class Fish(val homeX: Float, val homeY: Float, val length: Float, val index: Int, phase: Float) {
        var x = homeX
        var y = homeY
        var phase = phase
        var angle = (-PI / 2).toFloat()
    }

    /** Tide offset in meters (Peru: about ±0.3 m around the mean). */
    var tide = -0.3f
        private set
    var tideTarget = -0.3f
    var waveHeight = 1.2f

    /** Bumps whenever the tide moves, so the painted depth map knows to refresh. */
    var depthVersion = 0
        private set

    val staticDepth = FloatArray(GRID_W * GRID_H) { k ->
        depthAtMeanLevel((k % GRID_W + 0.5f) * CELL, (k / GRID_W + 0.5f) * CELL)
    }

    val waves = mutableListOf<Wave>()
    val tracers = List(TRACER_COUNT) { Tracer() }
    val fish = listOf(
        440f to 455f, 450f to 508f, 560f to 448f, 550f to 505f,
        472f to 545f, 532f to 548f, 512f to 470f
    ).mapIndexed { i, (hx, hy) -> Fish(hx, hy, 12f + random.nextFloat() * 6f, i, random.nextFloat() * 6.28f) }

    // Foam as parallel arrays: thousands of short-lived particles, no per-particle objects.
    val foamX = FloatArray(MAX_FOAM)
    val foamY = FloatArray(MAX_FOAM)
    val foamLife = FloatArray(MAX_FOAM)
    val foamMaxLife = FloatArray(MAX_FOAM)
    val foamRadius = FloatArray(MAX_FOAM)
    var foamCount = 0
        private set

    private var spawnTimer = 0f
    private var streamScale = 0f
    private var velU = 0f
    private var velV = 0f

    init {
        tracers.forEach(::respawn)
    }

    // Current shape for a unit stream scale, sampled on the depth grid; scaled by streamScale at runtime.
    // Precomputing keeps trig and exp out of the per-particle loops, which run thousands of times a frame.
    private val unitU = FloatArray(GRID_W * GRID_H)
    private val unitV = FloatArray(GRID_W * GRID_H)

    init {
        streamScale = 1f
        for (k in unitU.indices) {
            val x = (k % GRID_W + 0.5f) * CELL
            val y = (k / GRID_W + 0.5f) * CELL
            val e = 1.5f
            unitU[k] = (streamFunction(x, y + e) - streamFunction(x, y - e)) / (2f * e)
            unitV[k] = -(streamFunction(x + e, y) - streamFunction(x - e, y)) / (2f * e)
        }
        streamScale = 0f
    }

    /** Bilinear sample of a grid-centered field at world (x, y), clamped to the edges. */
    private fun sample(field: FloatArray, x: Float, y: Float): Float {
        val gx = (x / CELL - 0.5f).coerceIn(0f, GRID_W - 1.001f)
        val gy = (y / CELL - 0.5f).coerceIn(0f, GRID_H - 1.001f)
        val i = gx.toInt()
        val j = gy.toInt()
        val fx = gx - i
        val fy = gy - j
        val k = j * GRID_W + i
        val top = field[k] + (field[k + 1] - field[k]) * fx
        val bottom = field[k + GRID_W] + (field[k + GRID_W + 1] - field[k + GRID_W]) * fx
        return top + (bottom - top) * fy
    }

    fun depth(x: Float, y: Float) = sample(staticDepth, x, y) + tide

    /** 0.3..2: how strong the rip runs for the current tide and waves. */
    fun ripStrength(tideLevel: Float = tide): Float =
        ((waveHeight / 1.2f) * (1f - 0.9f * tideLevel)).coerceIn(0.3f, 2f)

    /** Runs the model for a few seconds so the first frame already shows waves, foam and current. */
    fun prewarm(seconds: Float = 16f) {
        val dt = 1f / 30f
        repeat((seconds / dt).toInt()) { step(dt) }
    }

    /** Eases the tide toward [tideTarget]; runs even while paused. Returns true if it moved. */
    fun updateTide(dt: Float): Boolean {
        if (abs(tideTarget - tide) <= 0.0005f) return false
        tide += (tideTarget - tide) * min(1f, dt * 2.5f)
        depthVersion++
        return true
    }

    fun step(dt: Float) {
        // Long frames are split so waves never jump over a sandbar.
        val substeps = if (dt > 1f / 40f) 2 else 1
        val h = dt / substeps
        repeat(substeps) {
            streamScale = JET_SPEED * JET_WIDTH * ripStrength()
            stepWaves(h)
            stepFoam(h)
            stepTracers(h)
            stepFish(h)
        }
    }

    /** Where the angler stands: on dry sand, so it moves with the tide. */
    fun anglerY(): Float {
        var y = 540f
        while (y < 676f) {
            if (depth(ANGLER_X, y) < -0.22f) return y + 10f
            y += 2f
        }
        return 662f
    }

    // --- Current ---

    private fun streamFunction(x: Float, y: Float): Float {
        if (y <= Y1 || y >= Y2) return 0f
        val dx = x - CX
        if (abs(dx) >= CELL_SPAN) return 0f
        val c = cos(PI.toFloat() * dx / (2f * CELL_SPAN))
        return streamScale * tanh(dx / JET_WIDTH) * c * c * sin(PI.toFloat() * (y - Y1) / (Y2 - Y1))
    }

    /** Velocity (u, v) in units/s into [velU], [velV]; negative v flows offshore. */
    private fun velocityAt(x: Float, y: Float) {
        velU = sample(unitU, x, y) * streamScale
        velV = sample(unitV, x, y) * streamScale
    }

    // --- Waves and foam ---

    private fun stepWaves(dt: Float) {
        spawnTimer -= dt
        if (spawnTimer <= 0f) {
            waves.add(Wave(waveHeight))
            spawnTimer = WAVE_PERIOD_S
        }
        for (w in waves) {
            var alive = 0
            for (i in 0 until w.size) {
                if (w.done[i]) continue
                val x = w.x(i)
                val y = w.ys[i]
                val d = depth(x, y)
                if (d <= 0.04f) {
                    w.done[i] = true
                    w.heights[i] = 0f
                    w.breaking[i] = 0f
                    continue
                }
                alive++
                velocityAt(x, y)
                // Shallow-water speed, slowed a little when running against the rip.
                val speed = max(8f, 22f * sqrt(d) + 0.25f * velV)
                w.ys[i] = y + speed * dt
                val maxHeight = BREAK_RATIO * d
                if (w.heights[i] >= maxHeight) {
                    w.heights[i] = maxHeight
                    w.breaking[i] = min(1f, w.breaking[i] + dt * 6f)
                } else {
                    w.breaking[i] = max(0f, w.breaking[i] - dt * 3f)
                }
                if (w.breaking[i] > 0.3f && random.nextFloat() < dt * 8f * (0.4f + w.heights[i])) {
                    spawnFoam(
                        x + (random.nextFloat() - 0.5f) * SAMPLE_DX,
                        w.ys[i] + random.nextFloat() * 6f,
                        w.heights[i]
                    )
                }
            }
            // Light smoothing keeps the crest continuous while it bends.
            for (i in 1 until w.size - 1) {
                if (!w.done[i] && !w.done[i - 1] && !w.done[i + 1]) {
                    w.ys[i] += 0.06f * (w.ys[i - 1] + w.ys[i + 1] - 2f * w.ys[i])
                }
            }
            w.alive = alive
        }
        waves.removeAll { it.alive == 0 }
    }

    private fun spawnFoam(x: Float, y: Float, height: Float) {
        if (foamCount >= MAX_FOAM) return
        val life = 2.5f + random.nextFloat() * 3.5f
        foamX[foamCount] = x
        foamY[foamCount] = y
        foamLife[foamCount] = life
        foamMaxLife[foamCount] = life
        foamRadius[foamCount] = 1f + random.nextFloat() * (1f + 1.6f * height)
        foamCount++
    }

    private fun stepFoam(dt: Float) {
        var i = 0
        while (i < foamCount) {
            velocityAt(foamX[i], foamY[i])
            foamX[i] += (velU + (random.nextFloat() - 0.5f) * 6f) * dt
            // Waves push foam toward the shore except where the rip is stronger.
            foamY[i] += (velV + 5f + (random.nextFloat() - 0.5f) * 6f) * dt
            foamLife[i] -= dt
            if (foamLife[i] <= 0f || depth(foamX[i], foamY[i]) <= 0f) {
                removeFoam(i)
            } else {
                i++
            }
        }
    }

    private fun removeFoam(i: Int) {
        val last = foamCount - 1
        foamX[i] = foamX[last]
        foamY[i] = foamY[last]
        foamLife[i] = foamLife[last]
        foamMaxLife[i] = foamMaxLife[last]
        foamRadius[i] = foamRadius[last]
        foamCount--
    }

    // --- Tracers ---

    private fun respawn(t: Tracer) {
        repeat(30) {
            val x = 60f + random.nextFloat() * 880f
            val y = Y1 + random.nextFloat() * (Y2 - Y1)
            if (depth(x, y) > 0.1f) {
                t.x = x
                t.y = y
                t.historyCount = 0
                t.life = 3f + random.nextFloat() * 6f
                return
            }
        }
        t.life = 0.2f
    }

    private fun stepTracers(dt: Float) {
        for (t in tracers) {
            velocityAt(t.x, t.y)
            t.x += velU * dt
            t.y += velV * dt
            t.life -= dt
            if (t.historyCount == TRACER_HISTORY) {
                t.history.copyInto(t.history, 0, 2, TRACER_HISTORY * 2)
                t.historyCount--
            }
            t.history[t.historyCount * 2] = t.x
            t.history[t.historyCount * 2 + 1] = t.y
            t.historyCount++
            if (t.life <= 0f || t.y < Y1 - 40f || depth(t.x, t.y) <= 0.05f) respawn(t)
        }
    }

    // --- Fish: wander near the channel edges and face into the current ---

    private fun stepFish(dt: Float) {
        for (f in fish) {
            f.phase += dt
            val tx = f.homeX + 18f * sin(f.phase * 0.4f + f.index)
            val ty = f.homeY + 10f * sin(f.phase * 0.27f + 2f * f.index)
            f.x += (tx - f.x) * dt * 0.8f
            f.y += (ty - f.y) * dt * 0.8f
            velocityAt(f.x, f.y)
            val want = if (hypot(velU, velV) > 4f) atan2(-velV, -velU) else atan2(ty - f.y, tx - f.x)
            val diff = atan2(sin(want - f.angle), cos(want - f.angle))
            f.angle += diff * min(1f, dt * 2f)
        }
    }

    /** Speed of a tracer's recent motion, for fading slow trails. */
    fun tracerSpeed(t: Tracer): Float {
        val n = t.historyCount
        if (n < 3) return 0f
        val dx = t.history[(n - 1) * 2] - t.history[(n - 3) * 2]
        val dy = t.history[(n - 1) * 2 + 1] - t.history[(n - 3) * 2 + 1]
        return hypot(dx, dy) * 30f
    }
}
