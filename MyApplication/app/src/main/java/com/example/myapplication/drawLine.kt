package com.example.myapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun drawLineScreen(onNavigateBack: () -> Unit) {
    // Danh sách điểm tọa độ trên bảng vẽ
    var pathPoints by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }
    var showMessage by remember { mutableStateOf(false) } // Trạng thái hiển thị thông báo

    fun convertPathToCommands(points: List<Pair<Float, Float>>): List<Int> {
        if (points.size < 2) return listOf(0) // Không có đường đi hoặc chỉ một điểm -> Dừng
        val commands = mutableListOf<Int>()

        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            val dx = curr.first - prev.first
            val dy = curr.second - prev.second

            // Sử dụng tỷ lệ để xác định hướng
            commands.add(
                when {
                    kotlin.math.abs(dy) > kotlin.math.abs(dx) && dy < 0 -> 1 // Tiến (lên trên)
                    kotlin.math.abs(dy) > kotlin.math.abs(dx) && dy > 0 -> 2 // Lùi (xuống dưới)
                    kotlin.math.abs(dx) > kotlin.math.abs(dy) && dx < 0 -> 3 // Quẹo trái (sang trái)
                    kotlin.math.abs(dx) > kotlin.math.abs(dy) && dx > 0 -> 4 // Quẹo phải (sang phải)
                    else -> {
                        println("Hướng không rõ ràng giữa điểm $prev và $curr")
                        0 // Không rõ ràng
                    }
                }
            )
        }
        commands.add(0) // Thêm lệnh dừng vào cuối
        return commands
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Draw Line", color = Color.White) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_back3),
                            contentDescription = "Back",
                            tint = Color.Unspecified
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent) // TopAppBar trong suốt            )

            )}
    ) { paddingValues ->
        paddingValues


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Hàng chứa nút "Trở về", "Xóa", và "Gửi"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {


                Button(onClick = { pathPoints = emptyList() }) {
                    Text("Làm mới")
                }

                Button(onClick = {
                    val commands = convertPathToCommands(pathPoints)
                    val database = FirebaseDatabase.getInstance()
                    val ref = database.getReference("Car_Control/Duong_Di")
                    ref.setValue(commands.joinToString(","))
                        .addOnSuccessListener {
                            showMessage = true // Hiển thị thông báo thành công
                        }
                }) {
                    Text("Gửi")
                }
            }

            // Hiển thị thông báo khi gửi thành công
            if (showMessage) {
                val commands = convertPathToCommands(pathPoints)
                Text(
                    text = "Đường đi đã được gửi thành công! Dữ liệu gửi đi: ${
                        commands.joinToString(
                            ","
                        )
                    }",
                    color = Color.Green,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
                // Tắt thông báo sau 3 giây
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(3000)
                    showMessage = false
                }
            }

            // Canvas để vẽ đường đi
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            // Thêm điểm mới khi người dùng chạm vào Canvas
                            pathPoints = pathPoints + Pair(tapOffset.x, tapOffset.y)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Vẽ đường đi
                    if (pathPoints.size > 1) {
                        val path = Path().apply {
                            moveTo(pathPoints.first().first, pathPoints.first().second)
                            for (point in pathPoints.drop(1)) {
                                lineTo(point.first, point.second)
                            }
                        }

                        drawPath(
                            path = path,
                            color = Color.Green,
                            style = Stroke(
                                width = 4.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }

                    // Vẽ các điểm
                    pathPoints.forEach { point ->
                        drawCircle(
                            color = Color.Red,
                            radius = 8.dp.toPx(),
                            center = Offset(point.first, point.second)
                        )
                    }
                }
            }
        }
    }
}
