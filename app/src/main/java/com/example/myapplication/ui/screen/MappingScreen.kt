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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    var mapData by remember { mutableStateOf<List<MapPoint>>(emptyList()) }
    var area by remember { mutableStateOf(0f) }
    var scanStatus by remember { mutableStateOf("Chưa quét") }
    var isScanning by remember { mutableStateOf(scanStatus != "mapping_completed") }

    val scope = rememberCoroutineScope()
    val database = FirebaseDatabase.getInstance()
    val dbReference = database.reference

    // Hàm tính diện tích sử dụng công thức Shoelace
    fun calculateArea(points: List<MapPoint>): Float {
//        if (points.size < 3) return 0f // Không đủ điểm để tính diện tích
        if (points.size < 3) {
            Toast.makeText(context, "Không đủ dữ liệu để tính diện tích!", Toast.LENGTH_SHORT).show()
            return 0f
        }

        var area = 0f
        val n = points.size
        for (i in 0 until n) {
            val j = (i + 1) % n
            area += points[i].x * points[j].y
            area -= points[j].x * points[i].y
        }
        return abs(area) / 2
    }

    // Hàm gửi lệnh lên Firebase (dựa vào mã lệnh)
//    fun updateButtonState(button: Int, database: DatabaseReference) {
//        val commands = listOf("0", "1", "2", "3", "4") // Các mã lệnh Dừng, Tiến, Lùi, Trái, Phải
//        val updates = mutableMapOf<String, Any>()
//
//        commands.forEach {
//            updates[it] = if (it == button.toString()) "true" else "false"
//        }
//
//        database.child("Car_Control/Control").updateChildren(updates).addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                Toast.makeText(context, "Lệnh đã được gửi thành công: $button", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(context, "Lỗi khi gửi lệnh: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
    fun updateButtonState(button: Int, controlRef: DatabaseReference) {
        val commands = listOf("0", "1", "2", "3", "4") // Các mã lệnh Dừng, Tiến, Lùi, Trái, Phải
        val updates = mutableMapOf<String, Any>()

        commands.forEach {
            updates[it] = if (it == button.toString()) "true" else "false"
        }

        controlRef.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Lệnh đã được gửi thành công: $button", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Lỗi khi gửi lệnh: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Lắng nghe dữ liệu từ Firebase
    fun listenToFirebase() {
        val mapRef = database.getReference("Data_test/mapData")
        val statusRef = database.getReference("Car_Support/Giao_Dien/Mapping")

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
                Toast.makeText(context, "Lỗi khi tải dữ liệu bản đồ: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Lắng nghe trạng thái quét

        // Lắng nghe trạng thái quét
        statusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(context, "Dữ liệu không tồn tại!", Toast.LENGTH_SHORT).show()
                    return
                }
                scanStatus = snapshot.getValue(String::class.java) ?: "Chưa quét"
                isScanning = scanStatus != "mapping_completed"
                if (scanStatus == "mapping_completed") {
                    area = calculateArea(mapData)
                    Toast.makeText(context, "Diện tích mặt đáy: $area cm²", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Lỗi khi tải trạng thái quét: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
//        statusRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                scanStatus = snapshot.getValue(String::class.java) ?: "Chưa quét"
//                if (scanStatus == "mapping_completed") {
//                    isScanning = false
//                    area = calculateArea(mapData)
//                    Toast.makeText(context, "Diện tích mặt đáy: $area cm²", Toast.LENGTH_LONG).show()
//                }
//            }

//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (!snapshot.exists()) {
//                    Toast.makeText(context, "Dữ liệu không tồn tại!", Toast.LENGTH_SHORT).show()
//                    return
//                }
//                val points = mutableListOf<MapPoint>()
//                for (pointSnapshot in snapshot.children) {
//                    val x = pointSnapshot.child("x").getValue(Float::class.java) ?: 0f
//                    val y = pointSnapshot.child("y").getValue(Float::class.java) ?: 0f
//                    val distance = pointSnapshot.child("distance").getValue(Float::class.java) ?: 0f
//                    val angle = pointSnapshot.child("angle").getValue(Float::class.java) ?: 0f
//                    points.add(MapPoint(x, y, distance, angle))
//                }
//                mapData = points
//            }



//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(context, "Lỗi khi tải trạng thái quét: ${error.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
    }

    // Hàm tự động quét
    fun autoMapping() {
        val sensorRef = database.getReference("Sensor")
        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val distance = snapshot.child("Distance").getValue(Float::class.java) ?: 0f
                val angle = snapshot.child("Angle").getValue(Float::class.java) ?: 0f

                if (distance < 20) {
                    if (distance < 0 || distance > 500) {
                        Toast.makeText(context, "Dữ liệu khoảng cách không hợp lệ!", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // Nếu có vật cản, rẽ trái hoặc phải
                    updateButtonState(if (angle < 90) 3 else 4, database.getReference("Car_Control/Control"))
                } else {
                    // Nếu không có vật cản, tiến lên
                    updateButtonState(1, database.getReference("Car_Control/Control"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Lỗi khi tự động quét: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Khởi chạy lắng nghe Firebase
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
                    mapData.take(100).forEach { point -> // Giới hạn số lượng điểm được vẽ
                        drawCircle(
                            color = Color.Red,
                            radius = 5f,
                            center = Offset(
                                x = point.x + size.width / 2,
                                y = point.y + size.height / 2
                            )
                        )
                    }
                }

            } else {
                Text(text = "Chưa có dữ liệu bản đồ", color = Color.Gray, fontSize = 16.sp)
            }
        }

//
//      Các nút điều khiển
        Button(
            onClick = {
                // Chỉ cho phép thay đổi trạng thái nếu trạng thái không bị khóa
                if (!isScanning) {
                    scope.launch {
                        // Bắt đầu quét
                        database.getReference("Car_Support/Giao_Dien/Mapping")
                            .setValue("true").await() // Ghi trạng thái "đang quét" lên Firebase
                        isScanning = true // Cập nhật trạng thái cục bộ
                        autoMapping() // Kích hoạt quá trình tự động quét
                    }
                } else {
                    scope.launch {
                        // Dừng quét
                        database.getReference("Car_Support/Giao_Dien/Mapping")
                            .setValue("false").await() // Ghi trạng thái "ngừng quét" lên Firebase
                        isScanning = false // Cập nhật trạng thái cục bộ
                        updateButtonState(0, database.getReference("Car_Control/Control")) // Dừng các lệnh điều khiển
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isScanning) Color.Red else Color.Blue)
        ) {
            Text(if (isScanning) "Dừng Quét" else "Bắt Đầu Quét", color = Color.White)
        }


        // Đặt trạng thái về false khi thoát giao diện
        DisposableEffect(Unit) {
            onDispose {
                scope.launch(Dispatchers.IO) {
                    dbReference.child("Car_Support/Giao_Dien").child("Mapping").setValue("false").await()
                }
            }
        }
        // Nút quay lại
        Button(
            onClick = {
                navController.navigate("main")
            },
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
