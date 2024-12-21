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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import okhttp3.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import okio.ByteString
import java.util.concurrent.TimeUnit

// Biến toàn cục
val database = FirebaseDatabase.getInstance()
val radarRef = database.getReference("Radar")
val codeNumberRef = database.getReference("Code_Number")
val codeGDRef = database.getReference("Code_Giaodien")
val lightRef = database.getReference("Light")

sealed class Screen {
    object Menu : Screen()
    object Radar : Screen()
    object DrawLine : Screen ()
    object CarControl : Screen()
    object ObstacleAvoidance : Screen()
    object RemoteControl : Screen()
    object LineTracking : Screen()
    object Mapping : Screen()
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

    // Hàm quay lại Menu
    val navigateBack = { currentScreen = Screen.Menu }

    when (currentScreen) {
        is Screen.Menu -> MainMenu(onNavigate = { currentScreen = it })
        is Screen.Radar -> RadarScreen(onNavigateBack = navigateBack)
        is Screen.CarControl -> CarControlScreen(onNavigateBack = navigateBack)
        is Screen.DrawLine -> EmptyScreen("Vẽ đường đi", onNavigateBack = navigateBack)
        is Screen.ObstacleAvoidance -> EmptyScreen("Chạy xe tránh vật cản", onNavigateBack = navigateBack)
        is Screen.RemoteControl -> EmptyScreen("Điều khiển xe bằng remote", onNavigateBack = navigateBack)
        is Screen.LineTracking -> EmptyScreen("Dò line", onNavigateBack = navigateBack)
        is Screen.Mapping -> EmptyScreen("Dò map", onNavigateBack = navigateBack)
    }
}

@Composable
fun MainMenu(onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Menu Chính",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        // Các item menu với hình ảnh minh họa
        MenuItem(imageRes = R.drawable.radar_icon, label = "Giao diện Radar") {
            onNavigate(Screen.Radar)
        }
        MenuItem(imageRes = R.drawable.car_control, label = "Điều khiển xe và camera") {
            onNavigate(Screen.CarControl)
        }
        MenuItem(imageRes = R.drawable.draw_line, label = "Vẽ đường đi") {
            onNavigate(Screen.DrawLine)
        }
        MenuItem(imageRes = R.drawable.avoid_icon, label = "Chạy xe tránh vật cản") {
            onNavigate(Screen.ObstacleAvoidance)
        }
        MenuItem(imageRes = R.drawable.remote_control, label = "Điều khiển xe bằng remote") {
            onNavigate(Screen.RemoteControl)
        }
        MenuItem(imageRes = R.drawable.line_tracking, label = "Dò line") {
            onNavigate(Screen.LineTracking)
        }
        MenuItem(imageRes = R.drawable.screenshot_icon, label = "Dò map") {
            onNavigate(Screen.Mapping)
        }
    }
}

@Composable
fun MenuItem(@DrawableRes imageRes: Int, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick)
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = label,
                modifier = Modifier
                    .size(60.dp)
                    .padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

data class RadarData(val KhoangCach: Int = 0, val Goc: Int = 0)

@Composable
fun RadarScreen(onNavigateBack: () -> Unit) {
    var radarDataList by remember { mutableStateOf<List<RadarData>>(emptyList()) }
    var sweepAngle by remember { mutableStateOf(180f) }
    var isScanning by remember { mutableStateOf(false) } // Biến trạng thái quét

    // Hàm khởi động quét
    fun startMeasurement() {
        radarDataList = emptyList() // Xóa dữ liệu cũ
        codeNumberRef.child("7").setValue("true")
        isScanning = true
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

class WebSocketClient(private val onImageReceived: (ByteArray) -> Unit) {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect(url: String) {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // Nhận dữ liệu ảnh và gọi callback
                onImageReceived(bytes.toByteArray())
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                t.printStackTrace()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println("WebSocket closed: $reason")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Goodbye!")
    }
}
@Composable
fun CarControlScreen(onNavigateBack: () -> Unit) { val context = LocalContext.current
    val imageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }

    // WebSocket client
    val webSocketClient = remember {
        WebSocketClient { byteArray ->
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            imageBitmap.value = bitmap.asImageBitmap()
        }
    }

    LaunchedEffect(Unit) {
        webSocketClient.connect("ws://3.233.123.220:3000")
    }

    DisposableEffect(Unit) {
        onDispose {
            webSocketClient.disconnect()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        // Nút Trở về
        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Text("Trở về")
        }

        // Hiển thị video stream
        Box(modifier = Modifier.align(Alignment.Center)) {
            imageBitmap.value?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = "Video Stream",
                    modifier = Modifier.fillMaxWidth()
                )
            } ?: Text(
                text = "Loading video stream...",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        // Các nút điều khiển
        Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Button Kèn
                Button(
                    onClick = {codeNumberRef.child("5").setValue("true")},
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.horn_icon), // Hình ảnh cái kèn
                        contentDescription = "Kèn",
                        modifier = Modifier.size(100.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Trái
                    Button(
                        onClick = { codeNumberRef.child("3").setValue("true")},
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                        modifier = Modifier
                            .width(70.dp)
                            .height(90.dp)
                    ) {}
                    // Phải
                    Button(
                        onClick = {codeNumberRef.child("4").setValue("true")},
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                        modifier = Modifier
                            .width(70.dp)
                            .height(90.dp)
                    ) {}
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Stop
                Button(
                    onClick = { codeNumberRef.child("0").setValue("true")},
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier
                        .width(70.dp)
                        .height(90.dp)
                ) {}

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Lên
                    Button(
                        onClick = {codeNumberRef.child("1").setValue("true")},
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                        modifier = Modifier
                            .width(70.dp)
                            .height(40.dp)
                    ) {}
                    // Xuống
                    Button(
                        onClick = { codeNumberRef.child("2").setValue("true")},
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

