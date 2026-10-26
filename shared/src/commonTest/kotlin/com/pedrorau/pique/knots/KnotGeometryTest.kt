package com.pedrorau.pique.knots

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KnotGeometryTest {

    private val line = listOf(listOf(0f, 0f, 0.5f), listOf(50f, 10f, 0.5f), listOf(100f, 0f, 0.5f))

    @Test
    fun sampleStrandKeepsEndpointsAndCount() {
        val sample = sampleStrand(line, count = 40)
        assertEquals(40, sample.size)
        assertTrue(abs(sample.x(0)) < 0.01f && abs(sample.y(0)) < 0.01f)
        assertTrue(abs(sample.x(39) - 100f) < 0.01f && abs(sample.y(39)) < 0.01f)
    }

    @Test
    fun sampleStrandSpacesPointsEvenly() {
        val sample = sampleStrand(line, count = 40)
        val gaps = (1 until sample.size).map { hypot(sample.x(it) - sample.x(it - 1), sample.y(it) - sample.y(it - 1)) }
        assertTrue(gaps.max() - gaps.min() < 0.2f, "gaps vary too much: ${gaps.min()}..${gaps.max()}")
    }

    @Test
    fun doubledStrandDrawsBothLinesAndTheTurn() {
        val sample = sampleStrand(listOf(listOf(0f, 0f, 0.5f, 4f), listOf(100f, 0f, 0.5f, 4f)), count = 20)
        val polyline = sample.toPolyline(StrandKind.DOUBLE)
        assertTrue(polyline.size > 40)
        // The two lines sit 4 units apart, one on each side of the centerline.
        assertTrue(abs(abs(polyline.xyz[1]) - 2f) < 0.01f)
        assertTrue(abs(abs(polyline.xyz[(polyline.size - 1) * 3 + 1]) - 2f) < 0.01f)
    }

    @Test
    fun blendHalfwayAveragesSteps() {
        val knot = Knot(
            id = "t", name = "t", category = "t", difficulty = "t", strength = "t", lines = "t",
            summary = "t", tip = "t",
            strands = listOf(StrandSpec("a", StrandKind.SINGLE, 10)),
            steps = listOf(
                KnotStep("a", listOf(listOf(listOf(0f, 0f), listOf(10f, 0f)))),
                KnotStep("b", listOf(listOf(listOf(0f, 20f), listOf(10f, 20f))))
            )
        )
        val mid = KnotFrames(knot).blend(0, 1, 0.5f).single()
        assertTrue((0 until mid.size).all { abs(mid.y(it) - 10f) < 0.01f })
    }
}
