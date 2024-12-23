package com.example.myapplication.ui.screen
import androidx.compose.foundation.layout.size
import android.graphics.drawable.Icon
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
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.platform.LocalDensity
import com.example.myapplication.AnglePicker
import com.example.myapplication.R
import com.example.myapplication.RadarView
import com.google.firebase.database.DatabaseReference


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappingScreen(onNavigateBack: () -> Unit) {
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
    val scope = rememberCoroutineScope()


    // Trạng thái cho chế độ radar toàn màn hình
    val isFullScreen = remember { mutableStateOf(false) }


    fun sendCommandToFirebase(commandNumber: Int, value: String) {
        coroutineScope.launch {
            try {
                // Gửi giá trị "true" hoặc "false" tới Firebase với key tương ứng
                dbReference.child("car_Control/Control").child(commandNumber.toString()).setValue(value).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gửi lệnh $commandNumber: $value", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gửi lệnh thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun sendAngleToFirebase(angle: Int) {
        coroutineScope.launch {
            try {
                // Gửi góc quét (angle) lên Firebase
                dbReference.child("Control/Angle").setValue(angle).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gửi góc quét: $angle°", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gửi lệnh thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // Lắng nghe dữ liệu khoảng cách từ Firebase khi bật chế độ tự động
    LaunchedEffect(isAutoMode.value) {
        if (isAutoMode.value) {
            dbReference.child("Radar/KhoangCach").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val distance = snapshot.getValue(Int::class.java) ?: 0
                    obstacleDistance.value = distance

                    // Logic tránh vật cản
                    if (distance in 1..20) {
                        // Lấy thêm dữ liệu từ các cảm biến góc trái và phải
                        dbReference.child("Sensor/LeftDistance").get().addOnSuccessListener { leftSnapshot ->
                            val leftDistance = leftSnapshot.getValue(Int::class.java) ?: Int.MAX_VALUE
                            dbReference.child("Sensor/RightDistance").get().addOnSuccessListener { rightSnapshot ->
                                val rightDistance = rightSnapshot.getValue(Int::class.java) ?: Int.MAX_VALUE

                                if (leftDistance > rightDistance && leftDistance > 20) {
                                    // Rẽ trái nếu khoảng cách bên trái lớn hơn
                                    scope.launch { updateButtonState(2, dbReference) } // Gửi lệnh rẽ trái
                                } else if (rightDistance > 20) {
                                    // Rẽ phải nếu khoảng cách bên phải lớn hơn
                                    scope.launch { updateButtonState(3, dbReference) } // Gửi lệnh rẽ phải
                                } else {
                                    // Nếu cả hai bên đều bị cản, dừng lại
                                    scope.launch { updateButtonState(0, dbReference) } // Gửi lệnh dừng
                                }
                            }
                        }
                    } else {
                        // Không có vật cản, tiếp tục đi thẳng
                        scope.launch { updateButtonState(1, dbReference) } // Gửi lệnh đi thẳng
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Tắt chế độ tự động, dừng xe
            scope.launch { updateButtonState(0, dbReference) } // Gửi lệnh dừng
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapping Screen", color = Color.White) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onNavigateBack() // Kích hoạt điều hướng trở lại
                        },
                        modifier = Modifier.background(Color.Transparent) // Làm nền trong suốt
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back), // Sử dụng biểu tượng tùy chỉnh
                            contentDescription = "Back",
                            tint = Color.White, // Chỉ định màu đen cho biểu tượng
                            modifier = Modifier.size(24.dp) // Kích thước biểu tượng
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent // Làm thanh TopAppBar trong suốt
                )

            )
        }
    ) { paddingValues ->

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
                                                sendAngleToFirebase(angle)
                                            }
                                        )
                                        Text(
                                            "Góc hiện tại: ${selectedAngle.value}°",
                                            fontSize = 14.sp
                                        )
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
//                                        sendCommandToFirebase(
//                                            "Control/Command",
//                                            "forward"
//                                        )
//                                        sendCommandToFirebase(1, true) // Gửi lệnh "Dừng"
                                            scope.launch { updateButtonState(1, dbReference) }

                                        },
                                        modifier = Modifier.size(50.dp),
                                        shape = CircleShape,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                                    ) { Text("↑", color = Color.White, fontSize = 20.sp) }
                                    Spacer(modifier = Modifier.height(8.dp))


                                    Row {
                                        Button(
//                                        onClick = {
////                                            sendCommandToFirebase("Control/Command", "left")
//                                            sendCommandToFirebase(3, true) // Gửi lệnh "Dừng"
//
//
//                                        }
                                            onClick = {
                                                scope.launch {
                                                    updateButtonState(
                                                        3,
                                                        dbReference
                                                    )
                                                }
                                            },

                                            modifier = Modifier
                                                .width(60.dp)
////                                            .height(60.dp)
////                                            .size(50.dp) // Giảm kích thước xuống 40.dp
                                                .clip(CircleShape)
                                                .background(
                                                    color = Color.White,
                                                    shape = CircleShape
                                                )
//                                        shape = CircleShape

                                        ) {
//                                        Text("←", color = Color.White, fontSize = 20.sp)
                                            Icon(
                                                painter = painterResource(id = com.example.myapplication.R.drawable.arrow_left),
                                                contentDescription = "Left",
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(
                                                        color = Color.Transparent,
                                                        shape = CircleShape
                                                    ),
                                                tint = Color.White
                                            )

                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = {
//                                            sendCommandToFirebase(
//                                                "Control/Command",
//                                                "stop"
//                                            )
//                                            sendCommandToFirebase(0, true) // Gửi lệnh "Dừng"
                                                scope.launch { updateButtonState(0, dbReference) }

                                            },
                                            modifier = Modifier.size(50.dp),
                                            shape = CircleShape,
                                        ) { Text("■", color = Color.White, fontSize = 20.sp) }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = {
//                                            sendCommandToFirebase(
//                                                "Control/Command",
//                                                "right"
//                                            )
//                                            sendCommandToFirebase(4, true) // Gửi lệnh "Dừng"
                                                scope.launch { updateButtonState(4, dbReference) }

                                            },
                                            modifier = Modifier.size(50.dp),
                                            shape = CircleShape,
                                        ) { Text("→", color = Color.White, fontSize = 20.sp) }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
//                                        sendCommandToFirebase(
//                                            "Control/Command",
//                                            "backward"
//                                        )
//                                        sendCommandToFirebase(2, true) // Gửi lệnh "Dừng"
                                            scope.launch { updateButtonState(2, dbReference) }

                                        },
                                        modifier = Modifier.size(50.dp),
                                        shape = CircleShape,
                                    ) { Text("↓", color = Color.White, fontSize = 20.sp) }
                                }
                            }

//                            // Nút quay lại
//                            Button(
//                                onClick = {
//                                    coroutineScope.launch {
//                                        try {
//                                            // Đặt 'Mapping' thành false khi nhấn nút
//                                            FirebaseDatabase.getInstance()
//                                                .getReference("Interface/avoidObject")
//                                                .setValue("false") // Sử dụng Boolean
//                                                .await()
//                                            Log.d(
//                                                "RadarScreen",
//                                                "Successfully reset 'Radar' to false"
//                                            )
//                                        } catch (e: Exception) {
//                                            Log.e(
//                                                "RadarScreen",
//                                                "Failed to reset 'Radar' to false",
//                                                e
//                                            )
//                                        }
//                                        onNavigateBack
////                                        navController.navigate("main")
//                                    }
//                                },
////                modifier = Modifier.fillMaxWidth()
//                                modifier = Modifier
//                                    .padding(top = 16.dp),
//                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
//                                shape = RoundedCornerShape(8.dp)
//                            ) {
//                                Text("Quay lại", color = Color.White)
//                            }
                        }

                    }
                }
            }
        }
    }
}






fun updateButtonState(button: Int, database: DatabaseReference) {
    val commands = listOf("0", "1", "2", "3", "4") // Các mã lệnh cho Dừng, Tiến, Lùi, Trái, Phải
    val updates = mutableMapOf<String, Any>()

    // Đặt trạng thái của nút được nhấn là "true", các nút khác là "false"
    commands.forEach {
        updates[it] = if (it == button.toString()) "true" else "false"
    }

    // Cập nhật trạng thái vào Firebase
    database.child("Car_Control/Control").updateChildren(updates).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("Firebase", "Trạng thái các nút đã được cập nhật (dưới dạng chuỗi)")
        } else {
            Log.e("Firebase", "Lỗi khi cập nhật trạng thái: ${task.exception}")
        }
    }
}
