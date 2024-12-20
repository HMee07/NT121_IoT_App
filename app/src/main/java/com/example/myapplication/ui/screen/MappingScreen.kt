// File: com/example/myapplication/ui/screen/MappingScreen.kt
package com.example.myapplication.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// Định nghĩa data class MapPoint
data class MapPoint(
    val x: Float,
    val y: Float,
    val distance: Float,
    val angle: Float
)

@Composable
fun MappingScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Trạng thái quét
    var isScanning by remember { mutableStateOf(false) }
    var mapData by remember { mutableStateOf<List<MapPoint>>(emptyList()) }
    var area by remember { mutableStateOf(0f) }
    var scanStatus by remember { mutableStateOf("Chưa quét") }

    // Hàm tính diện tích sử dụng công thức Shoelace
    fun calculateArea(points: List<MapPoint>): Float {
        if (points.size < 3) return 0f // Không đủ điểm để tính diện tích

        var area = 0f
        val n = points.size
        for (i in 0 until n) {
            val j = (i + 1) % n
            area += points[i].x * points[j].y
            area -= points[j].x * points[i].y
        }
        return abs(area) / 2
    }
    // Kết nối Firebase
    fun listenToFirebase() {
        val database = FirebaseDatabase.getInstance()
        val mapRef = database.getReference("mapData")
        val statusRef = database.getReference("status")
        val areaRef = database.getReference("area")

        // Lắng nghe dữ liệu map
        mapRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val points = mutableListOf<MapPoint>()
                for (pointSnapshot in snapshot.children) {
                    val x = pointSnapshot.child("x").getValue(Float::class.java) ?: 0f
                    val y = pointSnapshot.child("y").getValue(Float::class.java) ?: 0f
                    val distance = pointSnapshot.child("distance").getValue(Float::class.java) ?: 0f
                    val angle = pointSnapshot.child("angle").getValue(Float::class.java) ?: 0f
                    points.add(MapPoint(x, y, distance, angle))
                }
                mapData = points
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu cần
            }
        })

        // Lắng nghe trạng thái quét
        statusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scanStatus = snapshot.getValue(String::class.java) ?: "Chưa quét"
                if (scanStatus == "mapping_completed") {
                    isScanning = false
                    // Tính diện tích sau khi hoàn thành quét
                    area = calculateArea(mapData)
                    Toast.makeText(context, "Diện tích mặt đáy: $area cm²", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu cần
            }
        })

        // Lắng nghe diện tích
        areaRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                area = snapshot.getValue(Float::class.java) ?: 0f
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu cần
            }
        })
    }



    // Hàm gửi lệnh tới Firebase
    fun sendCommandToFirebase(command: String) {
        coroutineScope.launch {
            val database = FirebaseDatabase.getInstance()
            val commandRef = database.getReference("Control/Command")
            commandRef.setValue(command).addOnSuccessListener {
                Toast.makeText(context, "Gửi lệnh: $command thành công", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { exception ->
                Toast.makeText(context, "Gửi lệnh thất bại: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // Hàm bắt đầu quét
    fun startScanning() {
        isScanning = true
        scanStatus = "Đang quét..."
        mapData = emptyList()
        area = 0f
        // Gửi lệnh bắt đầu quét tới ESP32
        sendCommandToFirebase("start_mapping")
        // Có thể thêm logic kiểm tra khi nào quét hoàn thành
    }

    // Hàm dừng quét
    fun stopScanning() {
        isScanning = false
        scanStatus = "Đã dừng quét."
        // Gửi lệnh dừng quét tới ESP32
        sendCommandToFirebase("stop_mapping")
        // Tính diện tích sau khi dừng quét
        area = calculateArea(mapData)
        Toast.makeText(context, "Diện tích mặt đáy: $area cm²", Toast.LENGTH_LONG).show()
    }



    // Bắt đầu lắng nghe dữ liệu từ Firebase khi Composable được tạo
    LaunchedEffect(Unit) {
        listenToFirebase()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Phần hiển thị bản đồ
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (mapData.isNotEmpty()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Vẽ các điểm vật cản
                    mapData.forEach { point ->
                        drawCircle(
                            color = Color.Red,
                            radius = 5f,
                            center = Offset(
                                x = point.x + size.width / 2,
                                y = point.y + size.height / 2
                            )
                        )
                    }

                    // Vẽ đường kết nối các điểm
                    if (mapData.size > 1) {
                        for (i in 0 until mapData.size - 1) {
                            val start = Offset(
                                x = mapData[i].x + size.width / 2,
                                y = mapData[i].y + size.height / 2
                            )
                            val end = Offset(
                                x = mapData[i + 1].x + size.width / 2,
                                y = mapData[i + 1].y + size.height / 2
                            )
                            drawLine(
                                color = Color.Blue,
                                start = start,
                                end = end,
                                strokeWidth = 2f
                            )
                        }
                    }
                }
            } else {
                Text(text = "Chưa có dữ liệu bản đồ", color = Color.Gray, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Phần hiển thị trạng thái quét và diện tích
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Trạng thái: $scanStatus", fontSize = 18.sp, color = Color.Black)
            if (!isScanning && mapData.isNotEmpty()) {
                Text(text = "Diện tích mặt đáy: $area cm²", fontSize = 18.sp, color = Color.Green)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Phần các nút điều khiển
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    if (!isScanning) {
                        startScanning()
                    }
                },
                enabled = !isScanning,
                modifier = Modifier
                    .size(width = 120.dp, height = 50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("Bắt Đầu Quét", color = Color.White, fontSize = 16.sp)
            }

            Button(
                onClick = {
                    if (isScanning) {
                        stopScanning()
                    }
                },
                enabled = isScanning,
                modifier = Modifier
                    .size(width = 120.dp, height = 50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Dừng Quét", color = Color.White, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nút quay lại
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Quay lại", color = Color.White, fontSize = 16.sp)
        }
    }
}


