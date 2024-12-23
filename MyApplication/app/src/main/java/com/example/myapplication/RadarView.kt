package com.example.myapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

@Composable
fun RadarView(
    modifier: Modifier = Modifier.size(300.dp),
    sweepAngle: Int, // Góc quét của radar
    backgroundColor: Color = Color(0xFF00151A), // Màu nền đen pha xanh
    gridColor: Color = Color(0xFF00CFCF), // Màu lưới xanh nhạt
    sweepColor: Brush = Brush.radialGradient( // Gradient cho tia quét
        colors = listOf(Color(0xFF00CFCF).copy(alpha = 0.8f), Color.Transparent)
    )
) {
    //var sweepAngle by remember { mutableStateOf(0f) } // Góc quét radar
    val database = FirebaseDatabase.getInstance().reference
    val radarObjects = remember { mutableStateListOf<Offset>() } // Danh sách tọa độ vật thể



//    // Hiệu ứng quét động
//    LaunchedEffect(Unit) {
//        while (true) {
//            sweepAngle = (sweepAngle + 2) % 360f // Tăng góc quét
//            delay(200) // Điều chỉnh tốc độ quét
//        }
//    }
    // Đọc dữ liệu từ Firebase
    LaunchedEffect(Unit) {
        database.child("Radar").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val goc = snapshot.child("Goc").getValue(Float::class.java) ?: 0f
                val khoangCach = snapshot.child("KhoangCach").getValue(Float::class.java) ?: 0f

                // Chuyển đổi từ Góc và Khoảng Cách thành tọa độ X, Y
                val angleRad = Math.toRadians(goc.toDouble())
                val x = (khoangCach * Math.cos(angleRad)).toFloat()
                val y = (khoangCach * Math.sin(angleRad)).toFloat()

                // Cập nhật danh sách vật thể
                radarObjects.clear()
                radarObjects.add(Offset(x, -y)) // Đảo y vì hệ tọa độ Canvas
            }

            override fun onCancelled(error: DatabaseError) {
                println("Lỗi đọc dữ liệu từ Firebase: ${error.message}")
            }
        })
    }

    // Vẽ radar
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2) // Tâm radar
        val radius = size.minDimension / 2 // Bán kính radar
        val gridSpacing = radius / 10 // Khoảng cách nhỏ hơn giữa các ô lưới


        // Vẽ nền
        drawRect(color = backgroundColor, size = size)


        // Vẽ các vòng tròn đồng tâm
        for (i in 1..4) {
            drawCircle(
                color = gridColor.copy(alpha = 0.2f),
                radius = radius * i / 4,
                center = center,
                style = Stroke(width = 6f)
            )
        }
        // Vẽ các trục Đông-Tây-Nam-Bắc
        drawLine(
            color = gridColor.copy(alpha = 0.5f),
            start = Offset(center.x, 0f),
            end = Offset(center.x, size.height),
            strokeWidth = 3f
        )
        drawLine(
            color = gridColor.copy(alpha = 0.5f),
            start = Offset(0f, center.y),
            end = Offset(size.width, center.y),
            strokeWidth = 3f
        )

        // Vẽ ký tự N, S, E, W
        val textPaint = Paint().asFrameworkPaint().apply {
            color = android.graphics.Color.WHITE
            textSize = 48f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        drawContext.canvas.nativeCanvas.apply {
            drawText("N", center.x , center.y - radius - 20f, textPaint)
            drawText("S", center.x  , center.y + radius + 55f, textPaint)
            drawText("E", center.x + radius + 30f, center.y + 10f, textPaint)
            drawText("W", center.x - radius - 50f, center.y + 10f, textPaint)
        }

        // Vẽ lưới bổ sung (gồm các đường chia nhỏ)
        for (i in 1..8) {
            val angle = Math.toRadians(i * 45.0) // Mỗi 45 độ một đường
            val endX = center.x + radius * Math.cos(angle).toFloat()
            val endY = center.y + radius * Math.sin(angle).toFloat()

            drawLine(
                color = gridColor.copy(alpha = 0.3f), // Lưới mờ hơn
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 2f
            )
        }
        // Vẽ lưới ô vuông nhỏ
        for (x in 0 until size.width.toInt() step gridSpacing.toInt()) {
            drawLine(
                color = gridColor.copy(alpha = 0.2f),
                start = Offset(x.toFloat(), 0f),
                end = Offset(x.toFloat(), size.height),
                strokeWidth = 1f
            )
        }
        for (y in 0 until size.height.toInt() step gridSpacing.toInt()) {
            drawLine(
                color = gridColor.copy(alpha = 0.2f),
                start = Offset(0f, y.toFloat()),
                end = Offset(size.width, y.toFloat()),
                strokeWidth = 1f
            )
        }

        // Vẽ tia quét radar dựa trên sweepAngle
        drawArc(
            brush = sweepColor,
            startAngle = -90f, // Tia quét bắt đầu từ phía trên
            sweepAngle = sweepAngle.toFloat(),
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )

        // Vẽ vật thể trên radar
        radarObjects.forEach { offset ->
            drawCircle(
                color = Color.Red,
                radius = 10f,
                center = Offset(center.x + offset.x, center.y + offset.y)
            )
        }
    }
}
