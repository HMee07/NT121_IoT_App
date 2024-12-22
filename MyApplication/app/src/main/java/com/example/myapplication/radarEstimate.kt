package com.example.myapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


data class RadarData(val KhoangCach: Int = 0, val Goc: Int = 0)

fun calculateObjectWidth(objectData: List<RadarData>): Float {
    if (objectData.size < 2) return 0f
    val angleStart = Math.toRadians(objectData.first().Goc.toDouble())
    val angleEnd = Math.toRadians(objectData.last().Goc.toDouble())
    val rStart = objectData.first().KhoangCach.toFloat()
    val rEnd = objectData.last().KhoangCach.toFloat()

    val x1 = rStart * kotlin.math.cos(angleStart).toFloat()
    val y1 = rStart * kotlin.math.sin(angleStart).toFloat()
    val x2 = rEnd * kotlin.math.cos(angleEnd).toFloat()
    val y2 = rEnd * kotlin.math.sin(angleEnd).toFloat()

    return kotlin.math.sqrt((x2 - x1).let { it * it } + (y2 - y1).let { it * it })

}

//Tính bề rộng vật thể
@Composable
fun DisplayObjectInfo(radarDataList: List<RadarData>, modifier: Modifier = Modifier) {
    val objects = groupObjects(radarDataList)
    val objectInfo = objects.mapIndexed { index, obj ->
        val startAngle = obj.first().Goc
        val endAngle = obj.last().Goc
        val width = calculateObjectWidth(obj)
        Triple(index + 1, startAngle to endAngle, width)
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .background(Color.DarkGray, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        objectInfo.forEach { (id, angles, width) ->
            Column(modifier = Modifier.padding(8.dp)) {
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
            }
        }
    }
}

fun groupObjects(data: List<RadarData>, distanceThreshold: Int = 10): List<List<RadarData>> {
    val objects = mutableListOf<MutableList<RadarData>>()
    var currentObject = mutableListOf<RadarData>()

    data.sortedBy { it.Goc }.forEach { radarPoint ->
        if (currentObject.isEmpty() ||
            abs(radarPoint.KhoangCach - currentObject.last().KhoangCach) <= distanceThreshold) {
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

@Composable
fun RadarScreen(onNavigateBack: () -> Unit) {
    var radarDataList by remember { mutableStateOf<List<RadarData>>(emptyList()) }
    var sweepAngle by remember { mutableStateOf(180f) }
    var isScanning by remember { mutableStateOf(false) } // Biến trạng thái quét
    var currentAngle by remember { mutableStateOf(0) } // Biến lưu góc quét hiện tại từ Firebase

    // Hàm khởi động quét
    fun startMeasurement() {
        radarDataList = emptyList() // Xóa dữ liệu cũ
        radarRef.child("7").setValue("true")
        isScanning = true
        sweepAngle = 180f
    }

    // Lấy dữ liệu từ Firebase
    LaunchedEffect(Unit) {
        radarRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newRadarData = snapshot.getValue(RadarData::class.java)
                newRadarData?.let {
                    radarDataList = radarDataList + it
                    currentAngle = it.Goc
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error: ${error.message}")
            }
        })

        while (true) {
            sweepAngle -= 2f
            if (sweepAngle < 0f) sweepAngle = 180f
            delay(20)
        }
    }

    // UI hiển thị radar
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.BottomStart
    ) {
        // Vùng chứa các nút điều khiển
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically // Canh giữa theo chiều dọc
        ) {
            // Nút điều khiển
                Button(onClick = onNavigateBack, modifier = Modifier.padding(8.dp)) {
                    Text("Trở về")
                }
                Button(onClick = { startMeasurement() }, modifier = Modifier.padding(8.dp)) {
                    Text("Bắt đầu Đo")
                }
            // Hiển thị góc quét hiện tại
            Text(
                text = "Góc quét hiện tại: $currentAngle°",
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier
                    .background(Color.DarkGray, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )
        }
        // Vẽ radar
        Canvas(modifier = Modifier
            .size(250.dp)
            .align(Alignment.BottomStart)
            .padding(start = 200.dp)
        ){
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
            for (angle in 0..180 step 30) {
                val angleRadians = Math.toRadians(angle.toDouble())
                val endX = centerX + maxRadius * cos(angleRadians).toFloat()
                val endY = centerY - maxRadius * sin(angleRadians).toFloat()

                drawLine(
                    color = Color(0xFF00FF00),
                    start = Offset(centerX, centerY),
                    end = Offset(endX, endY),
                    strokeWidth = 2f
                )
            }

            // Hiệu ứng quét màu xanh từ tâm đến khoảng cách
            radarDataList.forEach { radarData ->
                val angleRadians = Math.toRadians(radarData.Goc.toDouble())
                val scaledDistance = (radarData.KhoangCach.coerceIn(0, maxDistance.toInt()) / maxDistance) * maxRadius

                // Vùng quét màu xanh
                val endXGreen = centerX + scaledDistance * cos(angleRadians).toFloat()
                val endYGreen = centerY - scaledDistance * sin(angleRadians).toFloat()

                drawLine(
                    color = Color(0x5400FF00),
                    start = Offset(centerX, centerY),
                    end = Offset(endXGreen, endYGreen),
                    strokeWidth = 3f
                )

                // Vùng sau vật cản màu đỏ
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
        // Hiển thị thông tin vật thể

    }
}