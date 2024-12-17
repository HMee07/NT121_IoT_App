package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.ui.component.AnglePicker
import com.example.myapplication.ui.component.ControlRadar
import com.example.myapplication.ui.component.RadarView
import com.example.myapplication.ui.component.WarningDisplay

@Composable
fun RadarScreen(navController: NavController) {
    var selectedAngle = remember { mutableIntStateOf(60) } // Góc quét mặc định
    var speed = remember { mutableStateOf(50f) }

    val objectCount = 8 // Giả lập số vật thể

    fun sendCommandToESP32(command: String) {
        println("Lệnh gửi tới ESP32: $command")
    }

    fun sendAngleToESP32(angle: Int) {
        println("Gửi góc quét $angle° tới ESP32")
    }

    fun sendSpeedToESP32(speed: Float) {
        println("Gửi tốc độ ${speed.toInt()}% tới ESP32")
    }

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Radar (Bên trái)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.Black)
        ) {
            RadarView(
                modifier = Modifier.fillMaxSize(),
                sweepAngle = selectedAngle.intValue // Góc quét được truyền vào từ AnglePicker
            )
        }
        // Các điều khiển (Bên phải)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hiển thị thông báo số lượng vật thể
            Text(
                text = "Có $objectCount vật thể đang ở xung quanh bạn!",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )

            AnglePicker(
                selectedAngle = selectedAngle.intValue,
                onAngleSelected = { newAngle ->
                    selectedAngle.intValue = newAngle // Cập nhật góc quét
                    println("Góc mới được chọn: $newAngle°") // Gửi hoặc xử lý góc mới
                }
            )

            // Điều khiển tốc độ của xe (thanh chỉnh tốc độ)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Tốc độ hiện tại: ${speed.value.toInt()}%", fontSize = 16.sp)

                // Thanh điều chỉnh tốc độ
                Slider(
                    value = speed.value,
                    onValueChange = { newSpeed ->
                        speed.value = newSpeed
                        sendSpeedToESP32(newSpeed) // Gửi tốc độ đến ESP32
                    },
                    valueRange = 0f..100f,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Nút quay lại
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Quay lại")
            }
        }

    }
}