package com.example.myapplication.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.ui.component.AnglePicker
import com.example.myapplication.ui.component.ControlRadar
import com.example.myapplication.ui.component.RadarView
import com.example.myapplication.ui.component.WarningDisplay
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun RadarScreen(navController: NavController) {
//    var selectedAngle = remember { mutableIntStateOf(60) } // Góc quét mặc định
//    var speed = remember { mutableStateOf(50f) }
//    val objectCount = 8 // Giả lập số vật thể
//
//
//    fun sendCommandToESP32(command: String) {
//        println("Lệnh gửi tới ESP32: $command")
//    }
//
//    fun sendAngleToESP32(angle: Int) {
//        FirebaseDatabase.getInstance().reference.child("Control/Angle").setValue(angle)
//        val context = null
//        Toast.makeText(context, "Gửi lệnh: $angle", Toast.LENGTH_SHORT).show()    }
//
//    fun sendSpeedToESP32(speed: Float) {
//        FirebaseDatabase.getInstance().reference.child("Control/Speed").setValue(speed)
//        val context = LocalContext.current
//        Toast.makeText(context, "Gửi lệnh: $speed", Toast.LENGTH_SHORT).show()    }
//    fun sendCommandToFirebase(command: String) {
//        FirebaseDatabase.getInstance().reference.child("Control/Command").setValue(command)
//        val context = null
//        Toast.makeText(context, "Gửi lệnh: $command", Toast.LENGTH_SHORT).show()
//    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedAngle = remember { mutableIntStateOf(60) } // Góc quét mặc định
    var speed = remember { mutableStateOf(50f) }
    val objectCount = 8 // Giả lập số vật thể

    // Hàm gửi dữ liệu tới Firebase Realtime Database
    suspend fun sendDataToFirebase(path: String, value: Any): Boolean {
        return try {
            FirebaseDatabase.getInstance().reference.child(path).setValue(value).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Hàm gửi lệnh tới Firebase và hiển thị Toast
    fun sendCommandToFirebase(command: String) {
        coroutineScope.launch {
            val success = sendDataToFirebase("Control/Command", command)
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "Gửi lệnh: $command thành công", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gửi lệnh thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Hàm gửi góc tới Firebase và hiển thị Toast
    fun sendAngleToESP32(angle: Int) {
        coroutineScope.launch {
            val success = sendDataToFirebase("Control/Angle", angle)
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "Gửi lệnh: $angle° thành công", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gửi lệnh thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Hàm gửi tốc độ tới Firebase và hiển thị Toast
    fun sendSpeedToESP32(speed: Float) {
        coroutineScope.launch {
            val success = sendDataToFirebase("Control/Speed", speed)
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "Gửi lệnh: ${speed.toInt()}% thành công", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gửi lệnh thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
        Column (
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(8.dp),
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


            // Nút chọn góc ở rìa bên phải
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnglePicker(
                    selectedAngle = selectedAngle.value,
                    onAngleSelected = { newAngle ->
                        selectedAngle.value = newAngle
                        sendAngleToESP32(newAngle)
                        sendCommandToFirebase("Set Angle: $newAngle")
                        println("Góc quét được chọn: $newAngle°")
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Góc hiện tại: ${selectedAngle.value}°",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

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
                        sendCommandToFirebase("Set Speed: ${newSpeed.toInt()}")
                    },
                    valueRange = 0f..100f,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Nút điều khiển xe
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxHeight(0.6f)
            ) {
                Button(
                    onClick = {sendCommandToFirebase("forward") },
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("↑", fontSize = 24.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { sendCommandToFirebase("left") },
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("←", fontSize = 24.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { sendCommandToFirebase("stop") },
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("■", fontSize = 24.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { sendCommandToFirebase("right") },
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("→", fontSize = 24.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { sendCommandToFirebase("backward") },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("↓", fontSize = 24.sp, color = Color.White)
                }
            }


//            AnglePicker(
//                selectedAngle = selectedAngle.intValue,
//                onAngleSelected = { newAngle ->
//                    selectedAngle.intValue = newAngle // Cập nhật góc quét
//                    println("Góc mới được chọn: $newAngle°") // Gửi hoặc xử lý góc mới
//                }
//            )



            // Nút quay lại
            Button(
                onClick = { navController.popBackStack() },
//                modifier = Modifier.fillMaxWidth()
                modifier = Modifier
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Quay lại",color = Color.White)
            }
        }

    }
}