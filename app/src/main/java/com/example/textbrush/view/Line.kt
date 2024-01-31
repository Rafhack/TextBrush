package com.example.textbrush.view

import android.graphics.RectF

data class Line(
    val segments: MutableList<Segment> = mutableListOf(),
    var fontSize: Float = 0f,
    val rect: RectF = RectF()
)
