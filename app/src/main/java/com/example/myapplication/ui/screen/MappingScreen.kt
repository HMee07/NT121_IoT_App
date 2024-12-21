@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication.ui.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class Obstacle(val angle: Float, val distance: Float)

@Composable
fun MappingScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = FirebaseDatabase.getInstance().getReference("Car_Support/Radar/Data")


    var obstacles by remember { mutableStateOf(listOf<Obstacle>()) }
    var isLoading by remember { mutableStateOf(true) }
    var area by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<Obstacle>()
                for (data in snapshot.children) {
                    val angleStr = data.key
//                    val distanceLong = data.getValue(Long::class.java) ?: data.getValue(Double::class.java)?.toString()
                    val distanceLong = data.getValue(Long::class.java) // Lấy giá trị dưới dạng Long

                    if (angleStr != null && distanceLong != null) {
                        val angle = angleStr.toFloatOrNull()
                        val distance = distanceLong.toFloat()
                        if (angle != null && distance != null) {
                            tempList.add(Obstacle(angle, distance))
                        }else {
                            Log.e("MappingScreen", "Invalid data: angle=$angleStr, distance=$distanceLong")
                        }
                    }
                    else {
                        Log.e("MappingScreen", "Missing angle or distance")
                    }
                }
                obstacles = tempList
                area = calculateArea(tempList)
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MappingScreen", "Failed to read data", error.toException())
                Toast.makeText(context, "Lỗi khi tải dữ liệu!", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapping Screen") },
                navigationIcon = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            try {
                                // Đặt 'Mapping' thành false khi nhấn nút
                                FirebaseDatabase.getInstance()
                                    .getReference("Car_Support/Giao_Dien/Mapping")
                                    .setValue("false") // Sử dụng Boolean
                                    .await()
                                Log.d("MappingScreen", "Successfully reset 'Mapping' to false")
                            } catch (e: Exception) {
                                Log.e("MappingScreen", "Failed to reset 'Mapping' to false", e)
                            }
//                        navController.popBackStack()
                        navController.navigate("main")
                    }

                    }) {
                        Icon(
                            painter = painterResource(id = com.example.myapplication.R.drawable.ic_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.weight(1f))
            } else {
                MapCanvas(obstacles = obstacles)
                Text(
                    text = "Diện tích vùng quét: $area cm²",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun MapCanvas(obstacles: List<Obstacle>) {
    Canvas(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2
        val maxDistance = obstacles.maxOfOrNull { it.distance } ?: 100f
        val radius = min(canvasWidth, canvasHeight) / 2 - 20.dp.toPx()

        // Vẽ vòng tròn radar
        drawCircle(
            color = Color.Green,
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
            style = Stroke(width = 4f)
        )

        // Vẽ các đường kinh tuyến (mỗi 45 độ)
        for (i in 0..360 step 45) {
            val angleRad = Math.toRadians(i.toDouble())
            val endX = centerX + radius * cos(angleRad).toFloat()
            val endY = centerY + radius * sin(angleRad).toFloat()
            drawLine(
                color = Color.LightGray,
                start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                end = androidx.compose.ui.geometry.Offset(endX, endY),
                strokeWidth = 2f
            )
        }

        // Vẽ các vật cản
        obstacles.forEach { obstacle ->
            val angleRad = Math.toRadians(obstacle.angle.toDouble())
            val distanceRatio = obstacle.distance / maxDistance
            val distancePx = distanceRatio * radius
            val x = centerX + distancePx * cos(angleRad).toFloat()
            val y = centerY + distancePx * sin(angleRad).toFloat()

            drawCircle(
                color = Color.Red,
                radius = 8f,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

fun calculateArea(obstacles: List<Obstacle>): Float {
    if (obstacles.size < 3) return 0f

    // Chuyển đổi từ góc và khoảng cách sang tọa độ X, Y
    val points = obstacles.map { obstacle ->
        val angleRad = Math.toRadians(obstacle.angle.toDouble())
        val x = (obstacle.distance * cos(angleRad)).toFloat()
        val y = (obstacle.distance * sin(angleRad)).toFloat()
        Pair(x, y)
    }

    // Tính diện tích bằng công thức Shoelace
    var area = 0f
    for (i in points.indices) {
        val (x1, y1) = points[i]
        val (x2, y2) = points[(i + 1) % points.size]
        area += (x1 * y2 - x2 * y1)
    }
    return kotlin.math.abs(area) / 2
}
