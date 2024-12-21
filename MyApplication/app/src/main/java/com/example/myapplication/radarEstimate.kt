package com.example.myapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay


data class RadarData(val KhoangCach: Int = 0, val Goc: Int = 0)

@Composable
fun RadarScreen(onNavigateBack: () -> Unit) {
    var radarDataList by remember { mutableStateOf<List<RadarData>>(emptyList()) }
    var sweepAngle by remember { mutableStateOf(180f) }
    var isScanning by remember { mutableStateOf(false) } // Biến trạng thái quét

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
        contentAlignment = Alignment.BottomCenter
    ) {
        // Nút điều khiển
        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Button(onClick = onNavigateBack, modifier = Modifier.padding(8.dp)) {
                Text("Trở về")
            }
            Button(onClick = { startMeasurement() }, modifier = Modifier.padding(8.dp)) {
                Text("Bắt đầu Đo")
            }
        }

        // Vẽ radar
        Canvas(modifier = Modifier.fillMaxSize()) {
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
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    topLeft = Offset(centerX - radius, centerY - radius)
                )
            }
            // Vẽ các vạch chia góc
            for (angle in 0..180 step 30) {
                val angleRadians = Math.toRadians(angle.toDouble())
                val endX = centerX + maxRadius * kotlin.math.cos(angleRadians).toFloat()
                val endY = centerY - maxRadius * kotlin.math.sin(angleRadians).toFloat()

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
                val endXGreen = centerX + scaledDistance * kotlin.math.cos(angleRadians).toFloat()
                val endYGreen = centerY - scaledDistance * kotlin.math.sin(angleRadians).toFloat()

                drawLine(
                    color = Color(0x5400FF00),
                    start = Offset(centerX, centerY),
                    end = Offset(endXGreen, endYGreen),
                    strokeWidth = 3f
                )

                // Vùng sau vật cản màu đỏ
                val endXRed = centerX + maxRadius * kotlin.math.cos(angleRadians).toFloat()
                val endYRed = centerY - maxRadius * kotlin.math.sin(angleRadians).toFloat()

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