package com.example.textbrush.view


import android.view.MotionEvent
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

data class Vector(
    var x: Float,
    var y: Float
) {

    override fun toString(): String {
        return "Vector: x = $x, y = $y"
    }

    companion object {

        private fun Vector.dist(vector: Vector): Float {
            return sqrt((vector.x - x).pow(2) + (vector.y - y).pow(2))
        }

        fun MotionEvent.toVector(): Vector = Vector(x, y)

        operator fun Vector.minus(vector: Vector) = copy().apply {
            this.x -= vector.x
            this.y -= vector.y
        }

        fun Vector.heading() = atan2(y, x) * (180F / PI.toFloat())

        fun Vector.mag() = sqrt(magSq())

        private fun Vector.magSq() = x * x + y * y

        fun Vector.lerp(other: Vector, spacing: Float): List<Vector> {
            val result = mutableListOf<Vector>()
            val distance = dist(other)

            if (spacing >= distance) return result

            val steps = (distance / spacing).toInt()

            for (i in 0 until steps + 1) {
                val factor = i.toFloat() / steps.toFloat()
                val x = this.x + factor * (other.x - this.x)
                val y = this.y + factor * (other.y - this.y)
                result.add(Vector(x, y))
            }

            return result
        }
    }
}
