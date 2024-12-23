package com.example.myapplication

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
// Data class lưu trữ thông tin radar
data class RadarData(val KhoangCach: Int = 0, val Goc: Int = 0)

// Hàm tính toán bề rộng của vật thể
fun calculateObjectWidth(objectData: List<RadarData>): Float {
    if (objectData.size < 2) return 0f

    var maxDistance = 0f
    for (i in objectData.indices) {
        for (j in i + 1 until objectData.size) {
            val angle1 = Math.toRadians(objectData[i].Goc.toDouble())
            val angle2 = Math.toRadians(objectData[j].Goc.toDouble())
            val r1 = objectData[i].KhoangCach.toFloat()
            val r2 = objectData[j].KhoangCach.toFloat()

            val x1 = r1 * cos(angle1).toFloat()
            val y1 = r1 * sin(angle1).toFloat()
            val x2 = r2 * cos(angle2).toFloat()
            val y2 = r2 * sin(angle2).toFloat()

            val distance = kotlin.math.sqrt((x2 - x1).let { it * it } + (y2 - y1).let { it * it })
            if (distance > maxDistance) maxDistance = distance
        }
    }

    return maxDistance
}

fun groupObjects(data: List<RadarData>, distanceThreshold: Int = 10, angleThreshold: Int = 5): List<List<RadarData>> {
    val objects = mutableListOf<MutableList<RadarData>>()
    var currentObject = mutableListOf<RadarData>()

    data.filter { it.KhoangCach > 0 } // Bỏ qua các điểm có KhoangCach == 0
        .sortedBy { it.Goc }
        .forEach { radarPoint ->
            if (currentObject.isEmpty() ||
                (abs(radarPoint.KhoangCach - currentObject.last().KhoangCach) <= distanceThreshold &&
                        abs(radarPoint.Goc - currentObject.last().Goc) <= angleThreshold)) {
                currentObject.add(radarPoint)
            } else {
                objects.add(currentObject)
                currentObject = mutableListOf(radarPoint)
            }
        }

    if (currentObject.isNotEmpty()) {
        objects.add(currentObject)
    }
    return objects
}

// Helper class Quadruple để chứa thông tin bổ sung
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
// Hàm hiển thị thông tin vật thể
@Composable
fun DisplayObjectInfo(radarDataList: List<RadarData>) {
    val objects = groupObjects(radarDataList)
    val objectInfo = objects.mapIndexed { index, obj ->
        val startAngle = obj.minOf { it.Goc } // Góc nhỏ nhất
        val endAngle = obj.maxOf { it.Goc }   // Góc lớn nhất
        val width = calculateObjectWidth(obj)
        val minDistance = obj.filter { it.KhoangCach > 0 }.minOfOrNull { it.KhoangCach } ?: 0
        Quadruple(index + 1, startAngle to endAngle, width, minDistance)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Cuộn dọc
            .padding(16.dp),
        horizontalAlignment = Alignment.End, // Căn về bên phải
        verticalArrangement = Arrangement.Top // Căn sát lên trên
    ) {
        objectInfo.forEachIndexed { index, (id, angles, width, minDistance) ->
            Box(
                modifier = Modifier
                    .padding(
                        top = if (index == 0) 0.dp else 8.dp, // Khung đầu tiên sát trên, khung tiếp theo cách nhau 8.dp
                        end = 8.dp // Căn sát lề phải
                    )
                    .background(Color.DarkGray, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "Vật thể $id:",
                        color = Color(0xFFFFA500), // Màu cam
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "- Góc: [${angles.first}° - ${angles.second}°]",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "- Bề rộng: ${"%.2f".format(width)} cm",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(text = "- Khoảng cách gần nhất: $minDistance cm",
                        color = Color.White,
                        fontSize = 14.sp )
                }
            }
        }
    }
}

// Hàm lắng nghe trạng thái Radar/7
fun observeRadarState(
    onScanningStateChange: (Boolean) -> Unit, // Callback khi trạng thái quét thay đổi
    onScanComplete: () -> Unit // Callback khi quét xong
) {
    val stRef = database.getReference("Car_Support/Radar/7")
    stRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val isRadarActive = snapshot.getValue(String::class.java)?.toBoolean() ?: false
            onScanningStateChange(isRadarActive)
            if (!isRadarActive) { // Khi trạng thái chuyển sang false
                onScanComplete()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            println("Error: ${error.message}")
        }
    })
}

@Composable
fun RadarScreen(onNavigateBack: () -> Unit) {
    var radarDataList by remember { mutableStateOf<List<RadarData>>(emptyList()) }
    val sweepAngle = remember { Animatable(0f) } // Bắt đầu từ 0 độ
    var isScanning by remember { mutableStateOf(false) }
    var showScanCompletedMessage by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Hàm khởi động quét
    fun startMeasurement() {
        radarDataList = emptyList()
        database.getReference("Car_Support/Radar/7").setValue("true")
        isScanning = true
        showScanCompletedMessage = false
        coroutineScope.launch {
            while (isScanning) {
                sweepAngle.animateTo(
                    targetValue = 170f,
                    animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
                )
                sweepAngle.snapTo(10f) // Reset về 10° sau mỗi lần quét
            }
        }
    }
    // Khởi chạy hiệu ứng quét
    LaunchedEffect(isScanning) {
        if (isScanning) {
            while (isScanning) {
                sweepAngle.animateTo(
                    targetValue = 180f, // Quét từ 0 đến 180 độ
                    animationSpec = tween(
                        durationMillis = 2000, // Thời gian để hoàn thành quét
                        easing = LinearEasing
                    )
                )
                sweepAngle.snapTo(0f) // Reset về 0 độ
            }
        } else {
            sweepAngle.snapTo(0f) // Dừng lại ở 0 độ khi quét kết thúc
        }
    }

    // Hàm dừng quét
    fun stopMeasurement() {
        isScanning = false
    }

    // Gọi lắng nghe trạng thái quét
    LaunchedEffect(Unit) {
        observeRadarState(
            onScanningStateChange = { state -> isScanning = state },
            onScanComplete = {
                showScanCompletedMessage = true
                coroutineScope.launch {
                    delay(3000) // Ẩn thông báo sau 3 giây
                    showScanCompletedMessage = false
                }
            }
        )
    }

    // Lắng nghe dữ liệu radar
    LaunchedEffect(Unit) {
        val radarDataRef = FirebaseDatabase.getInstance().getReference("Car_Support/Radar/Data")
        radarDataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newRadarDataList = mutableListOf<RadarData>()
                for (angleSnapshot in snapshot.children) {
                    val goc = angleSnapshot.key?.toIntOrNull() ?: continue // Lấy góc (key)
                    val khoangCach = angleSnapshot.getValue(Int::class.java) ?: 0 // Lấy khoảng cách (value)
                    newRadarDataList.add(RadarData(KhoangCach = khoangCach, Goc = goc))
                }
                radarDataList = newRadarDataList // Cập nhật danh sách dữ liệu radar
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Radar", "Lỗi khi lắng nghe Firebase: ${error.message}")
            }
        })
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Hàng chứa các nút điều khiển và góc quét
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Trở về")
            }
            Button(
                onClick = { startMeasurement() },
                enabled = !isScanning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) Color.Gray else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(if (isScanning) "Đang quét..." else "Bắt đầu Đo")
            }

            Text(
                text = "Góc quét hiện tại: 10° - 170°",
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier
                    .background(Color.DarkGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Hiển thị thông báo quét xong
        if (showScanCompletedMessage) {
            Text(
                text = "Đã quét xong",
                color = Color.Green,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.DarkGray, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                textAlign = TextAlign.Center
            )
            stopMeasurement()
        }

        // Radar và thông tin vật thể
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // Radar bên trái
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(30.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    val centerX = size.width / 2
                    val centerY = size.height
                    val maxRadius = size.height / 1.2f
                    val maxDistance = 50f

                    // Vẽ các vòng cung
                    for (i in 1..5) {
                        val radius = (i / 5f) * maxRadius
                        drawArc(
                            color = Color(0xFF00FF00),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            style = Stroke(width = 2f),
                            size = Size(radius * 2, radius * 2),
                            topLeft = Offset(centerX - radius, centerY - radius)
                        )
                    }
                    // Vẽ các vạch chia góc
                    for (angle in 0..180 step 15) { // Thêm vạch chia nhỏ hơn
                        val angleRadians = Math.toRadians(angle.toDouble())
                        val innerRadius = 0f // Tăng chiều dài của vạch cho các góc phụ
                        val outerRadius = maxRadius

                        val startX = centerX + innerRadius * cos(angleRadians).toFloat()
                        val startY = centerY - innerRadius * sin(angleRadians).toFloat()
                        val endX = centerX + outerRadius * cos(angleRadians).toFloat()
                        val endY = centerY - outerRadius * sin(angleRadians).toFloat()

                        drawLine(
                            color = Color(0xFF00FF00),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = if (angle % 30 == 0) 2f else 1f // Vạch chính dày hơn
                        )

                        // Đánh số góc chỉ ở các vạch chính (30°)
                        if (angle % 30 == 0) {
                            val textX = centerX + (outerRadius + 20) * cos(angleRadians).toFloat()
                            val textY = centerY - (outerRadius + 20) * sin(angleRadians).toFloat()

                            drawContext.canvas.nativeCanvas.drawText(
                                "$angle°",
                                textX,
                                textY,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor("#FF00FF00")
                                    textSize = 24f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )
                        }
                    }

                    // Hiệu ứng quét
                    drawArc(
                        color = Color(0x2F00FF00), // Màu xanh lá nhạt
                        startAngle = 180f, // Bắt đầu từ 0 độ
                        sweepAngle = sweepAngle.value, // Giá trị quét động
                        useCenter = true,
                        size = Size(maxRadius * 2, maxRadius * 2),
                        topLeft = Offset(centerX - maxRadius, centerY - maxRadius)
                    )

                    // Vẽ vật thể trên radar
                    radarDataList.forEach { radarData ->
                        val angleRadians = Math.toRadians(radarData.Goc.toDouble())
                        val scaledDistance =
                            (radarData.KhoangCach.coerceIn(0, maxDistance.toInt()) / maxDistance) * maxRadius

                        val endXGreen = centerX + scaledDistance * cos(angleRadians).toFloat()
                        val endYGreen = centerY - scaledDistance * sin(angleRadians).toFloat()

                        // Luôn vẽ đường xanh (góc quét)
                        drawLine(
                            color = Color(0xFF00FF00),
                            start = Offset(centerX, centerY),
                            end = Offset(endXGreen, endYGreen),
                            strokeWidth = 3f
                        )

                        // Chỉ vẽ đường đỏ khi khoảng cách > 0 (có vật thể)
                        if (radarData.KhoangCach > 0) {
                            val endXRed = centerX + maxRadius * cos(angleRadians).toFloat()
                            val endYRed = centerY - maxRadius * sin(angleRadians).toFloat()

                            drawLine(
                                color = Color.Red,
                                start = Offset(endXGreen, endYGreen),
                                end = Offset(endXRed, endYRed),
                                strokeWidth = 3f
                            )
                        }
                    }
                }
            }

            // Thông tin vật thể bên phải
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )
            {
                DisplayObjectInfo(radarDataList = radarDataList)
            }
        }
    }
}