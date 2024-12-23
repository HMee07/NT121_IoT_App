package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


data class RemoteControl(val KhoangCach: Int = 0, val Goc: Int = 0)

fun calculate_Object_Width(objectData: List<RemoteControl>): Float {
    if (objectData.size < 2) return 0f
    val angleStart = Math.toRadians(objectData.first().Goc.toDouble())
    val angleEnd = Math.toRadians(objectData.last().Goc.toDouble())
    val rStart = objectData.first().KhoangCach.toFloat()
    val rEnd = objectData.last().KhoangCach.toFloat()

    val x1 = rStart * kotlin.math.cos(angleStart).toFloat()
    val y1 = rStart * kotlin.math.sin(angleStart).toFloat()
    val x2 = rEnd * kotlin.math.cos(angleEnd).toFloat()
    val y2 = rEnd * kotlin.math.sin(angleEnd).toFloat()

    return kotlin.math.sqrt((x2 - x1).let { it * it } + (y2 - y1).let { it * it })

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(onNavigateBack: () -> Unit) {
    var radarDataList by remember { mutableStateOf<List<RemoteControl>>(emptyList()) }
    var sweepAngle by remember { mutableStateOf(180f) }
    var isScanning by remember { mutableStateOf(false) }
    var currentAngle by remember { mutableStateOf(0) }

    // Trạng thái menu
    var isMenuExpanded by remember { mutableStateOf(false) }

    // Trạng thái các chế độ
    var isRemoteControlEnabled by remember { mutableStateOf(false) }
    var isLineTrackingEnabled by remember { mutableStateOf(false) }
    var isAutoModeEnabled by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Hàm bật/tắt chế độ và gửi dữ liệu đến Firebase
    fun toggleMode(mode: String, isEnabled: Boolean) {
        coroutineScope.launch {
            try {
                FirebaseDatabase.getInstance()
                    .getReference("Interface/$mode")
                    .setValue(isEnabled.toString()).await()
                Log.d("RemoteScreen", "$mode set to $isEnabled")
            } catch (e: Exception) {
                Log.e("RemoteScreen", "Failed to set $mode: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Remote Control", color = Color.White) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    FirebaseDatabase.getInstance()
                                        .getReference("Interface/remoteControl")
                                        .setValue("false").await()
                                    Log.d("RemoteScreen", "Successfully reset 'Remote' to false")
                                } catch (e: Exception) {
                                    Log.e("RemoteScreen", "Failed to reset 'Remote' to false", e)
                                }
                            }
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
        ) {
            // Radar UI
            RadarView(
                radarDataList = radarDataList,
                sweepAngle = sweepAngle,
                modifier = Modifier.align(Alignment.Center)
            )

            // Nút menu ở góc phải
            FloatingActionButton(
                onClick = { isMenuExpanded = !isMenuExpanded },
                containerColor = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isMenuExpanded) R.drawable.icon_close else R.drawable.icon_menu
                    ),
                    contentDescription = if (isMenuExpanded) "Collapse Menu" else "Expand Menu",
                    tint = Color.Unspecified
                )
            }

            // Menu chế độ (Ẩn/Hiện dựa trên trạng thái)
            if (isMenuExpanded) {
                Column(
                    modifier = Modifier
//                        .fillMaxWidth()
                        .width(600.dp)
                        .align(Alignment.BottomCenter)
                        .background(Color.DarkGray.copy(alpha = 0.9f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Chế độ điều khiển",
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Nút bật/tắt chế độ Remote Control
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Điều khiển Remote", color = Color.White, fontSize = 16.sp)
                        Switch(
                            checked = isRemoteControlEnabled,
                            onCheckedChange = { isEnabled ->
                                isRemoteControlEnabled = isEnabled
                                toggleMode("remoteControl", isEnabled)
                            },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = Color.Green,
                                uncheckedThumbColor = Color.Red
                            )
                        )
                    }

                    // Nút bật/tắt chế độ Line Tracking
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dò Line", color = Color.White, fontSize = 16.sp)
                        Switch(
                            checked = isLineTrackingEnabled,
                            onCheckedChange = { isEnabled ->
                                isLineTrackingEnabled = isEnabled
                                toggleMode("scanLine", isEnabled)
                            },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = Color.Green,
                                uncheckedThumbColor = Color.Red
                            )
                        )
                    }

                    // Nút bật/tắt chế độ Auto Mode
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Chế độ Tự động", color = Color.White, fontSize = 16.sp)
                        Switch(
                            checked = isAutoModeEnabled,
                            onCheckedChange = { isEnabled ->
                                isAutoModeEnabled = isEnabled
                                toggleMode("avoidObject", isEnabled)
                            },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = Color.Green,
                                uncheckedThumbColor = Color.Red
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RadarView(
    radarDataList: List<RemoteControl>,
    sweepAngle: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(300.dp)
    ) {
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
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(centerX - radius, centerY - radius)
            )
        }

        // Vẽ các vạch chia góc
        for (angle in 0..180 step 30) {
            val angleRadians = Math.toRadians(angle.toDouble())
            val endX = centerX + maxRadius * cos(angleRadians).toFloat()
            val endY = centerY - maxRadius * sin(angleRadians).toFloat()

            drawLine(
                color = Color(0xFF00FF00),
                start = Offset(centerX, centerY),
                end = Offset(endX, endY),
                strokeWidth = 2f
            )
        }
    }
}
