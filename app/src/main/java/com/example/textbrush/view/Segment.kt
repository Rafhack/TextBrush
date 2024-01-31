package com.example.textbrush.view

import com.example.textbrush.view.Vector.Companion.lerp

data class Segment(
    var segmentStart: Vector,
    var segmentEnd: Vector,
    var heading: Float,
    val points: MutableList<Vector> = mutableListOf()
) {
    fun updatePoints(resolution: Float) {
        points.clear()
        points.addAll(segmentStart.lerp(segmentEnd, resolution))
        points.removeLastOrNull()
    }
}