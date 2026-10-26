package com.pedrorau.pique.knots

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A knot as stored in `composeResources/files/knots/<id>.json`.
 *
 * Geometry lives in a 100 × 70 box (y down). Each step gives every strand as control points
 * `[x, y, z]`, or `[x, y, z, spread]` for a doubled strand; z orders crossings (higher is on top)
 * and spread is the gap between the two lines of a doubled strand, widening it into an open loop.
 * Every step lists the same strands in the same order so one step can morph into the next.
 */
@Serializable
data class Knot(
    val id: String,
    val name: String,
    val category: String,
    val difficulty: String,
    val strength: String,
    val lines: String,
    val summary: String,
    val tip: String,
    val fixture: Fixture? = null,
    val strands: List<StrandSpec>,
    val steps: List<KnotStep>
)

/** The fixed object the line is tied to. */
@Serializable
data class Fixture(
    val kind: FixtureKind,
    val x: Float,
    val y: Float
)

@Serializable
enum class FixtureKind {
    @SerialName("hook") HOOK,
    @SerialName("ring") RING
}

@Serializable
data class StrandSpec(
    val name: String,
    val kind: StrandKind,
    /** Points the strand is resampled to; more for long or curly strands. */
    val samples: Int
)

@Serializable
enum class StrandKind {
    @SerialName("single") SINGLE,
    /** Two parallel lines joined by a round turn at the end: a bight. */
    @SerialName("double") DOUBLE
}

@Serializable
data class KnotStep(
    val text: String,
    val strands: List<List<List<Float>>>,
    /** Strand index → [from, to] fraction of its length drawn in the accent color. */
    val highlight: Map<String, List<Float>> = emptyMap()
)

@Serializable
internal data class KnotIndex(val knots: List<String>)
