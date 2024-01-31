package com.example.textbrush.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.withRotation
import com.example.textbrush.view.Vector.Companion.heading
import com.example.textbrush.view.Vector.Companion.mag
import com.example.textbrush.view.Vector.Companion.minus
import com.example.textbrush.view.Vector.Companion.toVector


class TextBrushView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val brushPaint = Paint().apply {
        isAntiAlias = true
        color = Color.LTGRAY
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        strokeWidth = STROKE_WIDTH
        typeface = Typeface.DEFAULT_BOLD
    }

    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private val lines: MutableList<Line> = mutableListOf()

    private var currentLine: Line = Line(fontSize = DEFAULT_FONT_SIZE)
    private var lineBeingResized: Line? = null
    private var isDrawing = false

    init {
        setLayerType(LAYER_TYPE_HARDWARE, brushPaint)
        setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            if (event.pointerCount <= 1) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> onMotionDown(event.toVector())
                    MotionEvent.ACTION_MOVE -> onMotionMove(event.toVector())
                    MotionEvent.ACTION_UP -> onMotionUp()
                }
            }
            performClick()
            true
        }
    }

    private fun onMotionUp() {
        if (isDrawing) {
            val lastLine = lines.lastOrNull() ?: return
            lastLine.rect.set(createLineRect(lastLine.segments))
        }
        lineBeingResized = null
        isDrawing = false
        invalidate()
    }

    private fun onMotionDown(touchVector: Vector) {
        startNewLine(touchVector)
        isDrawing = true
    }

    private fun onMotionMove(touchVector: Vector) {
        val lastSegment = currentLine.segments.lastOrNull() ?: return
        val diff = (touchVector - (lastSegment.segmentEnd))
        val resolution = currentLine.fontSize / TEXT_SEPARATION
        if (diff.mag() >= resolution) { // Creates the next segment only when this one is big enough
            lastSegment.segmentEnd = touchVector
            lastSegment.heading = diff.heading()
            lastSegment.updatePoints(resolution) // Then creates evenly spaced points inside of the segment, where the text will be written
            currentLine.segments.add(
                Segment(
                    segmentStart = lastSegment.segmentEnd,
                    segmentEnd = touchVector,
                    heading = diff.heading()
                ).also { it.updatePoints(resolution) }
            )
            invalidate()
        }
    }

    // Creates a rectangle around the text. The pinch-zoom focus must be inside this rectangle in order to change its line's font size
    private fun createLineRect(segments: List<Segment>): RectF {
        val leftMostSegment = (segments.map { it.segmentStart.x }).min()
        val topMostSegment = (segments.map { it.segmentStart.y }).min()
        val rightMostSegment = (segments.map { it.segmentStart.x }).max()
        val bottomMostSegment = (segments.map { it.segmentStart.y }).max()
        return RectF(
            leftMostSegment,
            topMostSegment,
            rightMostSegment,
            bottomMostSegment
        )
    }

    private fun startNewLine(startVector: Vector) {
        currentLine = Line(fontSize = DEFAULT_FONT_SIZE)
        currentLine.segments.add(Segment(startVector, startVector, 0f))
        lines.add(currentLine)
    }

    private fun drawChar(
        canvas: Canvas,
        text: Char,
        pointVector: Vector,
        segment: Segment,
        line: Line
    ) = with(canvas) {
        textPaint.textSize = line.fontSize
        withRotation(
            degrees = segment.heading,
            pivotX = pointVector.x,
            pivotY = pointVector.y
        ) {
            drawText(
                text.toString(),
                pointVector.x,
                pointVector.y,
                textPaint
            )
        }
    }

    private fun drawGuideLine(canvas: Canvas, segment: Segment) = with(canvas) {
        drawLine(
            segment.segmentStart.x,
            segment.segmentStart.y,
            segment.segmentEnd.x,
            segment.segmentEnd.y,
            brushPaint
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        lines.forEach { line ->
            var iteration = 0
            line.segments.forEach { segment ->
                if (isDrawing && line == lines.last()) { // Only draw the guide line for the last (current) line
                    drawGuideLine(canvas, segment)
                }

                segment.points.forEach { pointVector ->
                    val char = TEXT[(iteration++) % TEXT.length] // Wraps around to repeat the text
                    drawChar(canvas, char, pointVector, segment, line)
                }
            }
        }
    }

    fun undo() {
        lines.removeLastOrNull()
        invalidate()
    }

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (isDrawing) {
                isDrawing = false
                lines.removeLastOrNull() // Cancels the brush being drawn as the pinch gesture begins
            }

            val lineOnFocus = lineBeingResized ?: lines.firstOrNull {
                it.rect.contains(
                    detector.focusX,
                    detector.focusY
                )
            }
            lineBeingResized = lineOnFocus
            lineOnFocus?.run {
                if (detector.scaleFactor > 1) {
                    fontSize += detector.scaleFactor * SCALE_FACTOR_MULTIPLIER
                } else {
                    fontSize -= detector.scaleFactor * SCALE_FACTOR_MULTIPLIER
                }
                invalidate()
            }
            return true
        }
    }

    companion object {
        const val TEXT = "TEXT BRUSH " // Added a trailing blank space to separate the wrapping text
        const val TEXT_SEPARATION = 1.5f
        const val DEFAULT_FONT_SIZE = 70f
        const val SCALE_FACTOR_MULTIPLIER = 0.7f
        const val STROKE_WIDTH = 20f
    }
}