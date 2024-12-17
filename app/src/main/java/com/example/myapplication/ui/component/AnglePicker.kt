//package com.example.myapplication.ui.component
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.Button
////import androidx.compose.material3.Dialog
//import androidx.compose.material3.Text
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.window.Dialog
//import kotlin.math.cos
//import kotlin.math.sin
//
//@Composable
//fun AnglePicker(
//    selectedAngle: Int,
//    onAngleSelected: (Int) -> Unit
//) {
//    var isDialogOpen by remember { mutableStateOf(false) } // Mở hoặc đóng dialog
//
//    // Nút mở Dialog chọn góc
//    Button(
//        onClick = { isDialogOpen = true },
//        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
//        shape = CircleShape,
//        modifier = Modifier.size(80.dp)
//    ) {
//        Text(
//            text = "$selectedAngle°",
//            fontSize = 20.sp,
//            color = Color.White
//        )
//    }
//
//    // Dialog chứa hình tròn chọn góc
//    if (isDialogOpen) {
//        Dialog(onDismissRequest = { isDialogOpen = false }) {
//            Box(
//                modifier = Modifier
//                    .size(300.dp)
//                    .background(Color.White, CircleShape),
//                contentAlignment = Alignment.Center
//            ) {
//                val angles = listOf(60, 120, 240, 360) // Các góc
//                angles.forEachIndexed { index, angle ->
//                    val angleRad = Math.toRadians(-90.0 + index * 90.0)
//                    val offsetX = cos(angleRad).toFloat() * 100
//                    val offsetY = sin(angleRad).toFloat() * 100
//
//                    Box(
//                        modifier = Modifier
//                            .offset(x = offsetX.dp, y = offsetY.dp)
//                            .size(42.dp)
//                            .background(
//                                color = if (angle == selectedAngle) Color.Red else Color.LightGray,
//                                shape = CircleShape
//                            )
//                            .clickable {
//                                onAngleSelected(angle)
//                                isDialogOpen = false // Đóng Dialog
//                            },
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "$angle°",
//                            color = Color.White,
//                            fontSize = 14.sp
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
package com.example.myapplication.ui.component

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

//@Composable
//fun AnglePicker(
//    selectedAngle: Int,
//    onAngleSelected: (Int) -> Unit
//) {
//    var currentAngle by remember { mutableStateOf(selectedAngle) } // Lưu góc hiện tại
//    var isExpanded by remember { mutableStateOf(false) } // Trạng thái mở/đóng menu chọn góc
//
//    // Danh sách góc và màu tương ứng
//    val angles = listOf(60, 120, 240, 360)
//    val colors = listOf(Color.Gray, Color.Red, Color.Cyan, Color.Blue)
//
//    // Lấy mật độ màn hình
//    val density = LocalDensity.current
//    val radius = 120.dp // Bán kính vòng tròn lựa chọn góc
//
//    if (!isExpanded) {
//        // Trạng thái thu gọn: Chỉ hiển thị một hình tròn thể hiện góc hiện tại
//        Box(
//            modifier = Modifier
//                .size(80.dp)
//                .background(Color.LightGray, shape = CircleShape)
//                .clickable {
//                    isExpanded = true // Mở menu
//                },
//            contentAlignment = Alignment.Center
//        ) {
//            Text(text = "$currentAngle°", fontSize = 20.sp, color = Color.Black)
//        }
//    } else {
//        // Trạng thái mở rộng: Hiển thị vòng tròn chứa các tùy chọn góc
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .aspectRatio(1f)
//                .background(Color.Transparent),
//            contentAlignment = Alignment.Center
//        ) {
//            // Vẽ các phần hình quạt tương ứng với các góc
//            Canvas(modifier = Modifier.fillMaxSize()) {
//                val center = Offset(size.width / 2, size.height / 2) // Tâm vòng tròn
//                val radiusPx = with(density) { radius.toPx() } // Chuyển radius từ dp sang px
//                val segmentAngle = 360f / angles.size // Góc mỗi phần quạt
//
//                angles.forEachIndexed { index, angle ->
//                    drawArc(
//                        color = if (currentAngle == angle) colors[index] else colors[index].copy(alpha = 0.4f),
//                        startAngle = -90f + index * segmentAngle,
//                        sweepAngle = segmentAngle,
//                        useCenter = true,
//                        topLeft = Offset(center.x - radiusPx, center.y - radiusPx),
//                        size = androidx.compose.ui.geometry.Size(radiusPx * 2, radiusPx * 2)
//                    )
//                }
//            }
//
//            // Vị trí các tùy chọn góc (các nút)
//            angles.forEachIndexed { index, angle ->
//                // Tính toán góc và vị trí
//                val segmentAngle = 360f / angles.size
//                val middleAngle = -90.0 + index * segmentAngle + (segmentAngle / 2)
//                val angleRad = Math.toRadians(middleAngle)
//                val x = cos(angleRad).toFloat()
//                val y = sin(angleRad).toFloat()
//
//                // Chuyển vị trí thành dp
//                val offsetX = with(density) { (x * radius.toPx()).toDp() }
//                val offsetY = with(density) { (y * radius.toPx()).toDp() }
//
//                Box(
//                    modifier = Modifier
//                        .size(60.dp)
//                        .offset(x = offsetX, y = offsetY)
//                        .clickable {
//                            currentAngle = angle
//                            onAngleSelected(angle)
//                            isExpanded = false // Thu gọn menu sau khi chọn
//                        },
//                    contentAlignment = Alignment.Center
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(50.dp)
//                            .background(
//                                color = if (currentAngle == angle) Color.Black else Color.LightGray,
//                                shape = CircleShape
//                            ),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "$angle°",
//                            color = if (currentAngle == angle) Color.White else Color.Black,
//                            fontSize = 16.sp
//                        )
//                    }
//                }
//            }
//
//            // Nút trung tâm hiển thị góc hiện tại và có thể bấm để thu gọn
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center,
//                modifier = Modifier
//                    .size(80.dp)
//                    .background(Color.White, shape = CircleShape)
//                    .clickable {
//                        // Nếu nhấn vào nút trung tâm khi đang mở rộng
//                        // có thể thu gọn lại menu
//                        isExpanded = false
//                    }
//            ) {
//                Text(text = "Góc", fontSize = 16.sp, color = Color.Black)
//                Text(text = "$currentAngle°", fontSize = 20.sp, color = Color.Red)
//            }
//        }
//    }
//}
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

    val radius = 120.dp
    val density = LocalDensity.current
    val radiusPx = with(density) { radius.toPx() }

    if (!isExpanded) {
        // Trạng thái thu gọn: chỉ hiển thị hình tròn nhỏ với góc hiện tại
        Box(
            modifier = Modifier
                .size(80.dp)
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
                    .size(60.dp)
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
