package com.example.myapplication

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError


sealed class Screen {
    object Menu : Screen()
    object Radar : Screen()
    object DrawLine : Screen ()
    object CarControl : Screen()
    object ObstacleAvoidance : Screen()
    object RemoteControl : Screen()
    object LineTracking : Screen()
    object Screenshot : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                AppNavigator()
            }
        }
    }
}
@Composable
fun AppNavigator() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }

    // Hàm trở về Menu
    val navigateBack = { currentScreen = Screen.Menu }

    when (currentScreen) {
        is Screen.Menu -> MainMenu(onNavigate = { currentScreen = it })
        is Screen.Radar -> RadarScreen(onNavigateBack = navigateBack)
        is Screen.CarControl -> CarControlScreen(onNavigateBack = navigateBack)
        is Screen.DrawLine -> EmptyScreen("Vẽ đường đi", onNavigateBack = navigateBack)
        is Screen.ObstacleAvoidance -> EmptyScreen("Chạy xe tránh vật cản", onNavigateBack = navigateBack)
        is Screen.RemoteControl -> EmptyScreen("Điều khiển xe bằng remote", onNavigateBack = navigateBack)
        is Screen.LineTracking -> EmptyScreen("Dò line", onNavigateBack = navigateBack)
        is Screen.Screenshot -> EmptyScreen("Chụp màn hình và lưu Drive", onNavigateBack = navigateBack)
    }
}

@Composable
fun MainMenu(onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Menu Chính", style = MaterialTheme.typography.headlineMedium, color = Color.Black)
        Button(onClick = { onNavigate(Screen.Radar) }) { Text("Giao diện Radar") }
        Button(onClick = { onNavigate(Screen.CarControl) }) { Text("Điều khiển xe và camera") }
        Button(onClick = { onNavigate(Screen.DrawLine) }) { Text("Vẽ đường đi") }
        Button(onClick = { onNavigate(Screen.ObstacleAvoidance) }) { Text("Chạy xe tránh vật cản") }
        Button(onClick = { onNavigate(Screen.RemoteControl) }) { Text("Điều khiển xe bằng remote") }
        Button(onClick = { onNavigate(Screen.LineTracking) }) { Text("Dò line") }
        Button(onClick = { onNavigate(Screen.Screenshot) }) { Text("Chụp màn hình và lưu Drive") }
    }
}
data class RadarData(val KhoangCach: Int = 0, val Goc: Int = 0)

@Composable
fun RadarScreen(onNavigateBack: () -> Unit) {
    var radarDataList by remember { mutableStateOf<List<RadarData>>(emptyList()) }
    var sweepAngle by remember { mutableStateOf(180f) }
    val database = FirebaseDatabase.getInstance()
    val radarRef = database.getReference("Radar")
    val codeNumberRef = database.getReference("Code_Number")

    // Hàm khởi động quét
    fun startMeasurement() {
        radarDataList = emptyList() // Xóa dữ liệu cũ
        codeNumberRef.setValue(7) // Gửi Code_Number lên Firebase
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

@Composable
fun CarControlScreen(onNavigateBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Nút trở về
        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopStart)
        ) {
            Text("Trở về")
        }
    }
    Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        // Các nút điều khiển bên trái
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {},//BỎ SỰ KIỆN VÀO ĐÂY
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                modifier = Modifier
                    .width(70.dp)
                    .height(90.dp)
            ) {}
            Button(
                onClick = {},
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                modifier = Modifier
                    .width(70.dp)
                    .height(90.dp)
            ) {}
        }
        // Điều khiển bên phải
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Nút STOP lớn nằm bên trái
            Button(
                onClick = {  },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .width(70.dp)
                    .height(90.dp)
            ) {}
            // UP và DOWN nằm bên phải
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                    modifier = Modifier
                        .width(70.dp)
                        .height(40.dp)
                ) {
                }
                Button(
                    onClick ={},
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                    modifier = Modifier
                        .width(70.dp)
                        .height(40.dp)
                ) {}
            }
        }
    }
}

@Composable
fun EmptyScreen(title: String, onNavigateBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Nút trở về
        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopStart)
        ) {
            Text("Trở về")
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}

