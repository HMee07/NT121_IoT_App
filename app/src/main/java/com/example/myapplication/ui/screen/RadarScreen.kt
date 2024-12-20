package com.example.myapplication.ui.screen

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController


@Composable
fun RadarScreen(navController: NavController) {
    // Context và coroutine scope
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()



    // State quản lý giao diện
    val selectedAngle = remember { mutableIntStateOf(60) }
    val speed = remember { mutableFloatStateOf(50f) }
    val animatedSpeed = animateFloatAsState(targetValue = speed.value)
    val isControlsVisible = remember { mutableStateOf(false) } // Trạng thái ẩn/hiện nút điều khiển



    val isAutoMode = remember { mutableStateOf(false) }
    val obstacleDistance = remember { mutableStateOf(0) }
    val objectCount = remember { mutableStateOf(8) }

    // Firebase Realtime Database
    val database = FirebaseDatabase.getInstance()
    val dbReference = database.reference

    // Trạng thái cho chế độ radar toàn màn hình
    val isFullScreen = remember { mutableStateOf(false) }


    // Hàm gửi dữ liệu tới Firebase
    fun sendCommandToFirebase(path: String, value: Any) {
        coroutineScope.launch {
            try {
                dbReference.child(path).setValue(value).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gửi lệnh: $value", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gửi lệnh thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // Lắng nghe dữ liệu khoảng cách từ Firebase khi bật chế độ tự động
    LaunchedEffect(isAutoMode.value) {
        if (isAutoMode.value) {
            dbReference.child("Sensor/Distance").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val distance = snapshot.getValue(Int::class.java) ?: 0
                    obstacleDistance.value = distance

                    // Logic tránh vật cản
                    if (distance in 1..20) {
                        sendCommandToFirebase("Control/Command", "stop")
                    } else {
                        sendCommandToFirebase("Control/Command", "forward")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            sendCommandToFirebase("Control/Command", "stop")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
//            .verticalScroll(rememberScrollState()) // Kích hoạt cuộn cho màn hình
    ) {
        if (isFullScreen.value) {
            // Chế độ radar toàn màn hình
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                RadarView(
                    modifier = Modifier.fillMaxSize(),
                    sweepAngle = 60 // Ví dụ góc quét cố định
                )
                // Nút thoát chế độ toàn màn hình
                Button(
                    onClick = { isFullScreen.value = false }, // Thoát chế độ toàn màn hình
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("X", color = Color.White, fontSize = 20.sp)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Radar (Bên trái)
                Box(
                    modifier = Modifier
                        .weight(0.5f)
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
                        .weight(0.5f)
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row {

                        Column {
                            Spacer(modifier = Modifier.height(30.dp))

                            Slider(
                                value = animatedSpeed.value,
                                onValueChange = { newSpeed ->
                                    speed.value = newSpeed
                                    // Gửi dữ liệu tới Firebase hoặc cập nhật trạng thái
                                },
                                valueRange = 0f..100f,
                                steps = 100,
                                modifier = Modifier
                                    .height(30.dp) // Giới hạn chiều cao
                                    .width(100.dp)   // Đặt chiều rộng cố định
                                    .rotate(-90f)   // Xoay thành thanh đứng
                            )
                            Spacer(modifier = Modifier.height(38.dp))

//                       SimpleVerticalSlider()
                            Text(
                                text = "Tốc độ hiện tại: ${animatedSpeed.value.toInt()}%",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }

                        Column {

                            // Hiển thị thông báo số lượng vật thể
                            Text(
                                text = "Có ${objectCount.value} vật thể đang ở xung quanh bạn!",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            )

                            // Hàng chọn góc và chế độ tự động
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Chọn góc
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    AnglePicker(
                                        selectedAngle = selectedAngle.value,
                                        onAngleSelected = { angle ->
                                            selectedAngle.value = angle
                                            sendCommandToFirebase("Control/Angle", angle)
                                        }
                                    )
                                    Text("Góc hiện tại: ${selectedAngle.value}°", fontSize = 14.sp)
                                }

                                // Chế độ tự động
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Switch(
                                        checked = isAutoMode.value,
                                        onCheckedChange = { isChecked ->
                                            isAutoMode.value = isChecked
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.Green,
                                            uncheckedThumbColor = Color.Red
                                        )
                                    )
                                    Text("Chế độ tự động", fontSize = 14.sp)

                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                    // Nút hiển thị/ẩn các nút điều khiển
                                    Button(
                                        onClick = {
                                            isControlsVisible.value = !isControlsVisible.value
                                        }, // Toggle trạng thái
                                        modifier = Modifier.size(50.dp),
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            if (isControlsVisible.value) "-" else "+",
                                            fontSize = 20.sp,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Nút mở rộng radar full màn hình
                                    Button(
                                        onClick = {
                                            isFullScreen.value = true
                                        }, // Bật chế độ toàn màn hình
                                        modifier = Modifier.size(50.dp),
                                        shape = CircleShape,
                                    ) {
                                        Text("⤢", color = Color.White, fontSize = 20.sp)
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }

                    // Điều khiển xe
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Spacer(modifier = Modifier.height(16.dp))
                        if (isControlsVisible.value) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                Button(
                                    onClick = {
                                        sendCommandToFirebase(
                                            "Control/Command",
                                            "forward"
                                        )
                                    },
                                    modifier = Modifier.size(50.dp),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                                ) { Text("↑", color = Color.White, fontSize = 20.sp) }
                                Spacer(modifier = Modifier.height(8.dp))


                                Row {
                                    Button(
                                        onClick = {
                                            sendCommandToFirebase("Control/Command", "left")

                                        },
                                        modifier = Modifier.size(50.dp),
                                        shape = CircleShape,
                                    ) { Text("←", color = Color.White, fontSize = 20.sp) }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            sendCommandToFirebase(
                                                "Control/Command",
                                                "stop"
                                            )
                                        },
                                        modifier = Modifier.size(50.dp),
                                        shape = CircleShape,
                                    ) { Text("■", color = Color.White, fontSize = 20.sp) }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            sendCommandToFirebase(
                                                "Control/Command",
                                                "right"
                                            )
                                        },
                                        modifier = Modifier.size(50.dp),
                                        shape = CircleShape,
                                    ) { Text("→", color = Color.White, fontSize = 20.sp) }
                                }
                                Spacer(modifier = Modifier.height(8.dp))


                                Button(
                                    onClick = {
                                        sendCommandToFirebase(
                                            "Control/Command",
                                            "backward"
                                        )
                                    },
                                    modifier = Modifier.size(50.dp),
                                    shape = CircleShape,
                                ) { Text("↓", color = Color.White, fontSize = 20.sp) }
                            }
                        }

//

                        // Nút quay lại
                        Button(
                            onClick = { navController.popBackStack() },
//                modifier = Modifier.fillMaxWidth()
                            modifier = Modifier
                                .padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Quay lại", color = Color.White)
                        }
                    }

                }
            }
        }
    }
}

//
//@Composable
//fun SimpleVerticalSlider() {
//    var speed = remember { mutableStateOf(50f) }
//    val animatedSpeed = animateFloatAsState(targetValue = speed.value)
//
//    Column(
//        modifier = Modifier
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//
//    ) {
//        Spacer(modifier = Modifier.width(16.dp))
//
//
//        Slider(
//            value = animatedSpeed.value,
//            onValueChange = { newSpeed ->
//                speed.value = newSpeed
//                // Gửi dữ liệu tới Firebase hoặc cập nhật trạng thái
//            },
//            valueRange = 0f..100f,
//            steps = 100,
//            modifier = Modifier
//                .height(30.dp) // Giới hạn chiều cao
//                .width(150.dp)   // Đặt chiều rộng cố định
//                .rotate(-90f)   // Xoay thành thanh đứng
//        )
//        Spacer(modifier = Modifier.width(150.dp))
//
//        Text(
//            text = "Tốc độ hiện tại: ${animatedSpeed.value.toInt()}%",
//            fontSize = 16.sp,
//            color = Color.Black
//        )
//
//    }
//}


//@Preview(showBackground = true)
//@Composable
//fun PreviewSettingsScreen() {
//    val mockNavController = rememberNavController() // Mock NavController
//    RadarScreen(navController = mockNavController)}



