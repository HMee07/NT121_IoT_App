
package com.example.myapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun AnglePicker(
    selectedAngle: Int,
    onAngleSelected: (Int) -> Unit
) {
    var currentAngle by remember { mutableStateOf(selectedAngle) }
    var isExpanded by remember { mutableStateOf(false) }

    val angles = listOf(60, 120, 240, 360)
    val segmentCount = angles.size
    val segmentAngle = 360f / segmentCount

    val radius = 80.dp
    val density = LocalDensity.current
    val radiusPx = with(density) { radius.toPx() }

    if (!isExpanded) {
        // Trạng thái thu gọn: chỉ hiển thị hình tròn nhỏ với góc hiện tại
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Color.LightGray, shape = CircleShape)
                .clickable {
                    isExpanded = true // Mở menu chọn góc
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "$currentAngle°", fontSize = 20.sp, color = Color.Black)
        }
    } else {
        // Trạng thái mở rộng: hiển thị vòng tròn lớn chứa các tùy chọn góc
        Box(
            modifier = Modifier
                .size(radius * 2)
                .background(Color.Transparent)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Tính toán phần cung được chọn
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val dx = offset.x - centerX
                        val dy = offset.y - centerY
                        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                        if (distance <= radiusPx) {
                            val touchAngle = Math.toDegrees(kotlin.math.atan2(dy, dx).toDouble()).toFloat()
                            val normalizedAngle = (touchAngle + 450) % 360
                            val index = (normalizedAngle / segmentAngle).toInt() % segmentCount
                            val chosenAngle = angles[index]
                            currentAngle = chosenAngle
                            onAngleSelected(chosenAngle)
                            isExpanded = false // Thu gọn menu sau khi chọn
                        } else {
                            // Nếu click ra ngoài vòng tròn, có thể thu gọn menu
                            isExpanded = false
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)

                // Vẽ các phần cung tròn
                angles.forEachIndexed { index, angle ->
                    val startAngle = -90f + index * segmentAngle
                    val partColor = if (currentAngle == angle) {
                        Color.Gray
                    } else {
                        Color.Black.copy(alpha = 0.6f)
                    }

                    drawArc(
                        color = partColor,
                        startAngle = startAngle,
                        sweepAngle = segmentAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radiusPx, center.y - radiusPx),
                        size = androidx.compose.ui.geometry.Size(radiusPx * 2, radiusPx * 2)
                    )
                }

                // Vẽ các đường phân chia giữa các cung
                for (i in angles.indices) {
                    val lineAngle = Math.toRadians((-90f + i * segmentAngle).toDouble())
                    val lineX = center.x + radiusPx * cos(lineAngle).toFloat()
                    val lineY = center.y + radiusPx * sin(lineAngle).toFloat()

                    drawLine(
                        color = Color.White,
                        start = center,
                        end = Offset(lineX, lineY),
                        strokeWidth = 2f
                    )
                }

                // Vẽ text vào giữa mỗi cung
                angles.forEachIndexed { index, angle ->
                    val textAngleDeg = -90f + index * segmentAngle + segmentAngle / 2
                    val textAngleRad = Math.toRadians(textAngleDeg.toDouble())
                    val textRadius = radiusPx * 0.5f
                    val textX = center.x + textRadius * cos(textAngleRad).toFloat()
                    val textY = center.y + textRadius * sin(textAngleRad).toFloat()

                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = with(density) { 18.sp.toPx() }
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }

                    val textBounds = android.graphics.Rect()
                    paint.getTextBounds("$angle°", 0, "$angle°".length, textBounds)
                    val textHeight = textBounds.height()

                    drawContext.canvas.nativeCanvas.drawText(
                        "$angle°",
                        textX,
                        textY + textHeight / 2,
                        paint
                    )
                }
            }

            // Vòng tròn trung tâm hiển thị góc hiện tại
            // Có thể nhấn vào để thu gọn menu nếu muốn
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .clickable {
                        isExpanded = false // Thu gọn nếu không muốn chọn góc
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "${currentAngle}°", color = Color.Red, fontSize = 20.sp)
            }
        }
    }
}
