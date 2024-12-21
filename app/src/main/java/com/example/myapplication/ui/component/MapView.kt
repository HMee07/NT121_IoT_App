package com.example.myapplication.ui.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class Obstacle(val angle: Float, val distance: Float)

class MapView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val mapPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val obstaclePaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private val boundaryPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val obstacles = mutableListOf<Obstacle>()
    private var boundaryPath: List<Pair<Float, Float>> = listOf()

    // Tỷ lệ chuyển đổi từ cm sang pixels
    private val scaleFactor = 2.0f // Điều chỉnh theo nhu cầu

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val radius = min(width, height) / 2 * 0.8f
        val centerX = width / 2
        val centerY = height / 2

        // Vẽ vòng tròn bản đồ
        canvas.drawCircle(centerX, centerY, radius, mapPaint)

        // Vẽ các vật cản
        for (obstacle in obstacles) {
            val angleRad = Math.toRadians(obstacle.angle.toDouble())
            val distancePx = obstacle.distance * scaleFactor
            val x = centerX + distancePx * cos(angleRad).toFloat()
            val y = centerY + distancePx * sin(angleRad).toFloat()
            canvas.drawCircle(x, y, 8f, obstaclePaint)
        }

        // Vẽ đường biên nếu có
        if (boundaryPath.isNotEmpty()) {
            val boundaryPoints = boundaryPath.map { Pair(it.first * scaleFactor + centerX, it.second * scaleFactor + centerY) }
            for (i in 0 until boundaryPoints.size) {
                val start = boundaryPoints[i]
                val end = boundaryPoints[(i + 1) % boundaryPoints.size]
                canvas.drawLine(start.first, start.second, end.first, end.second, boundaryPaint)
            }
        }
    }

    fun updateObstacles(newObstacles: List<Obstacle>) {
        obstacles.clear()
        obstacles.addAll(newObstacles)
        invalidate() // Yêu cầu vẽ lại view
    }

    fun setBoundary(path: List<Pair<Float, Float>>) {
        boundaryPath = path
        invalidate()
    }
}
