package com.example.myapplication

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class WebSocketClient(
    private val onOpen: () -> Unit,
    private val onError: (Throwable) -> Unit,
    private val onClose: () -> Unit,
    private val onImageReceived: (ByteArray) -> Unit
) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect(url: String) {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                onOpen()
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                onImageReceived(bytes.toByteArray())
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onError(t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                onClose()
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Goodbye!")
        webSocket = null
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CarControlScreen(onNavigateBack: () -> Unit) {
    val imageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }
    val logMessage = remember { mutableStateOf("Initializing WebSocket...") } // Log trạng thái
    val speedValue = remember { mutableStateOf(75f) } // Giá trị mặc định của slider (0-100)
    val lightValue = remember { mutableStateOf(75f) } // Giá trị mặc định của slider (0-100)

    // Trạng thái menu
    var isMenuExpanded by remember { mutableStateOf(false) }

    // Trạng thái các chế độ
    var isRemoteControlEnabled by remember { mutableStateOf(false) }
    var isLineTrackingEnabled by remember { mutableStateOf(false) }
    var isAutoModeEnabled by remember { mutableStateOf(false) }
    // Hàm bật/tắt chế độ và gửi dữ liệu đến Firebase
    val coroutineScope = rememberCoroutineScope()

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


    // WebSocket client
    val webSocketClient = remember {
        WebSocketClient(
            onOpen = { logMessage.value = "WebSocket connected!" },
            onError = { error -> logMessage.value = "WebSocket error: ${error.message}" },
            onClose = { logMessage.value = "WebSocket disconnected!" },
            onImageReceived = { byteArray ->
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                if (bitmap != null) {
                    imageBitmap.value = bitmap.asImageBitmap()
                    logMessage.value = "Received image, size: ${byteArray.size} bytes"
                } else {
                    logMessage.value = "Failed to decode image"
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        // Kết nối đến WebSocket server
        try {
            webSocketClient.connect("ws://3.233.123.220:3000")
        } catch (e: Exception) {
            logMessage.value = "Failed to connect: ${e.message}"
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webSocketClient.disconnect()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control", color = Color.Black) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    FirebaseDatabase.getInstance()
                                        .getReference("Interface/cameraControl")
                                        .setValue("false").await()
                                    Log.d("ControlScreen", "Successfully reset 'Control' to false")
                                } catch (e: Exception) {
                                    Log.e("ControlScreen", "Failed to reset 'Control' to false", e)
                                }
                            }
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_back2),
                            contentDescription = "Back",
                            tint = Color.Unspecified
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
//                .background(Color.White)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xACCBEE), // Mã màu #accbee
                            Color(0xE7F0FD)  // Mã màu #e7f0fd
                        ),
                        start = Offset(0f, Float.POSITIVE_INFINITY), // Gradient bắt đầu từ đáy
                        end = Offset(0f, 0f) // Gradient kết thúc ở đỉnh
                    )
                )


        )
        // Đặt nền thành màu đen)
        {
            Image(
                painter = painterResource(id = R.drawable.bg_2),
                contentDescription = "BG",
                modifier = Modifier.fillMaxSize()
                    .width(2560.dp)
                    .height(1600.dp)
            )
            // Hiển thị video stream hoặc thông báo log
            Box(
                modifier = Modifier
//                    .size(.dp) // Kích thước Box cố định
                    .width(450.dp)
                    .height(350.dp)
                    .align(Alignment.Center)
                    .background(Color.Gray) // Màu nền cho Box
            ) {
                imageBitmap.value?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Video Stream",
                        modifier = Modifier.fillMaxSize() // Hiển thị đầy Box
                    )
                } ?: Text(
                    text = logMessage.value, // Hiển thị log trạng thái
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

//            // Nút trở về
//            Button(
//                onClick = onNavigateBack,
//                modifier = Modifier
//                    .align(Alignment.TopStart)
//                    .padding(8.dp)
//            ) {
//                Text("Trở về")
//            }

            // Các nút điều khiển
            Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Column (
                        modifier = Modifier
                        .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally)
                    {
                        Row(verticalAlignment = Alignment.CenterVertically, // Đảm bảo căn giữa theo chiều dọc
                            modifier = Modifier
                                .padding(end = 16.dp) // Đẩy toàn bộ qua bên trái màn hình
                                ) {
                            // Slider Toc đo
                                Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
//                                .align(Alignment.Start)  // Căn góc trên bên phải
                                    .padding(
                                        end = 16.dp,
//                                        top = 16.dp
                                    ) // Đẩy Slider sát hơn vào bên phải
                            ) {
                                // Slider
                                Slider(
                                    value = speedValue.value,
                                    onValueChange = { newValue ->
                                        speedValue.value = newValue
                                        controlRef.child("Toc_Do").setValue(newValue.toInt())
                                    },
                                    valueRange = 0f..100f,
                                    modifier = Modifier.width(150.dp).rotate(-90f)
                                        .padding(bottom = 20.dp)
                                        .padding(start = 24.dp)// Khoảng cách giữa Icon và Slider // Độ rộng Slider cố định
                                )
                                Spacer(modifier = Modifier.height(24.dp)) // Tạo khoảng trống giữa Slider và Icon

                                    Row ( verticalAlignment = Alignment.CenterVertically, // Đảm bảo các thành phần căn giữa theo chiều dọc
                                        horizontalArrangement = Arrangement.spacedBy(16.dp) // Tạo khoảng cách giữa các thành phần
                                                 ){
                                        // Icon nhỏ hình cây đèn
                                        Image(
                                            painter = painterResource(id = R.drawable.icon_speed),
                                            contentDescription = "Light Icon",
                                            modifier = Modifier
                                                .size(90.dp)
                                                .padding(end = 8.dp) // Khoảng cách giữa Icon và Slider
                                        )

                                        // Nút Kèn
                                        Button(
                                            onClick = {
                                                controlRef.child("Ken_Xe").setValue("true")
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFFF3BD) // Màu vàng
                                            ),
                                            modifier = Modifier.size(70.dp), // Đặt size để tạo hình tròn
                                            shape = RoundedCornerShape(50) // Bo tròn toàn bộ nút
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.horn_icon),
                                                contentDescription = "Kèn",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                            }
                            Spacer(modifier = Modifier.padding(end = 16.dp))
//                            // Nút Kèn
//                            Button(
//                                onClick = {
//                                    controlRef.child("Ken_Xe").setValue("true")
//                                },
//                                colors = ButtonDefaults.buttonColors(
//                                    containerColor = Color(0xFFFFF3BD) // Màu vàng
//                                ),
//                                modifier = Modifier.size(70.dp), // Đặt size để tạo hình tròn
//                                shape = RoundedCornerShape(50) // Bo tròn toàn bộ nút
//                            ) {
//                                Image(
//                                    painter = painterResource(id = R.drawable.horn_icon),
//                                    contentDescription = "Kèn",
//                                    modifier = Modifier.fillMaxSize()
//                                )
//                            }
                        }
                        Spacer(modifier = Modifier.padding(end=16.dp))


                        // Hàng chứa các nút điều khiển Trái/Phải
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            // Nút Trái
                            Button(
                                onClick = {
//                                controlRef.child("Cotrol/3").setValue("true")
                                },
                                shape = RoundedCornerShape(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF1E3A8A
                                    )
                                ),
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(90.dp)
                                    .pointerInteropFilter { motionEvent ->
                                        when (motionEvent.action) {
                                            android.view.MotionEvent.ACTION_DOWN -> {
                                                // Gửi lệnh "3" khi nhấn giữ nút
                                                Lenhref.child("Lenh").setValue("3,")
                                                true
                                            }

                                            android.view.MotionEvent.ACTION_UP -> {
                                                // Gửi lệnh "3,0" khi thả nút
                                                Lenhref.child("Lenh").setValue("3,0")
                                                true
                                            }

                                            else -> false
                                        }
                                    }
                            ) {}

                            // Nút Phải
                            Button(
                                onClick = {
//                                controlRef.child("Cotrol/4").setValue("true")
                                },
                                shape = RoundedCornerShape(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF1E3A8A
                                    )
                                ),
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(90.dp)
                                    .pointerInteropFilter { motionEvent ->
                                        when (motionEvent.action) {
                                            android.view.MotionEvent.ACTION_DOWN -> {
                                                // Gửi lệnh "4" khi nhấn giữ nút
                                                Lenhref.child("Lenh").setValue("4,")
                                                true
                                            }

                                            android.view.MotionEvent.ACTION_UP -> {
                                                // Gửi lệnh "4,0" khi thả nút
                                                Lenhref.child("Lenh").setValue("4,0")
                                                true
                                            }

                                            else -> false
                                        }
                                    }
                            ) {}

                        }
                    }
                }
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
                            .height(500.dp)
//                            .align(Alignment.BottomCenter)
                            .align(Alignment.Center)
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
                // Các nút điều khiển bên phải với Slider
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding( 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {


                        // Slider đèn
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .align(Alignment.Start)  // Căn góc trên bên phải
                                .padding(end = 8.dp, top = 16.dp) // Đẩy Slider sát hơn vào bên phải
                        ) {
                            // Icon nhỏ hình cây đèn
                            Image(
                                painter = painterResource(id = R.drawable.light_icon),
                                contentDescription = "Light Icon",
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(end = 8.dp) // Khoảng cách giữa Icon và Slider
                            )
                            // Slider
                            Slider(
                                value = lightValue.value,
                                onValueChange = { newValue ->
                                    lightValue.value = newValue
                                    controlRef.child("Den_Xe").setValue(newValue.toInt())
                                },
                                valueRange = 0f..100f,
                                modifier = Modifier.width(110.dp) // Độ rộng Slider cố định
                            )
                        }
                        // Nút chụp hình
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB0B0B0) // Màu vàng nhạt
                            ),
                            modifier = Modifier.size(70.dp), // Đặt size chiều rộng và cao bằng nhau để hình tròn
                            shape = RoundedCornerShape(50) // Bo tròn 50 để tạo hình tròn
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.take_icon),
                                contentDescription = "Chụp",
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Hàng chứa các nút điều khiển
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.align(Alignment.End) // Đẩy hàng nút sang cạnh phải
                        ) {
                            // Nút Stop
                            Button(
                                onClick = {
//                                controlRef.child("Cotrol/0").setValue("true")
                                },
                                shape = RoundedCornerShape(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDC2626)
                                ),
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(90.dp)
                                    .pointerInteropFilter { motionEvent ->
                                        when (motionEvent.action) {
                                            android.view.MotionEvent.ACTION_DOWN -> {
                                                // Gửi lệnh "0" khi nhấn giữ nút
                                                Lenhref.child("Lenh").setValue("0,")
                                                true
                                            }
                                            android.view.MotionEvent.ACTION_UP -> {
                                                // Gửi lệnh "0" khi thả nút
                                                Lenhref.child("Lenh").setValue("0,")
                                                true
                                            }
                                            else -> false
                                        }}
                            ) {}

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Nút Lên
                                Button(
                                    onClick = {
//                                        controlRef.child("Cotrol/1").setValue("true")
                                              },
                                    shape = RoundedCornerShape(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                    modifier = Modifier
                                        .width(75.dp)
                                        .height(40.dp)
                                        .pointerInteropFilter { motionEvent ->
                                            when (motionEvent.action) {
                                                android.view.MotionEvent.ACTION_DOWN -> {
                                                    // Gửi lệnh "1" khi nhấn giữ nút
                                                    Lenhref.child("Lenh").setValue("1,")
                                                    true
                                                }

                                                android.view.MotionEvent.ACTION_UP -> {
                                                    // Gửi lệnh "1,0" khi thả nút
                                                    Lenhref.child("Lenh").setValue("1,0")
                                                    true
                                                }

                                                else -> false
                                            }

                                        }) {}

                                // Nút Xuống
                                Button(
                                    onClick = {
//                                        controlRef.child("Cotrol/2").setValue("true")
                                              },
                                    shape = RoundedCornerShape(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFF1E3A8A
                                        )
                                    ),
                                    modifier = Modifier
                                        .width(75.dp)
                                        .height(40.dp)
                                        .pointerInteropFilter { motionEvent ->
                                            when (motionEvent.action) {
                                                android.view.MotionEvent.ACTION_DOWN -> {
                                                    // Gửi lệnh "2" khi nhấn giữ nút
                                                    Lenhref.child("Lenh").setValue("2,")
                                                    true
                                                }
                                                android.view.MotionEvent.ACTION_UP -> {
                                                    // Gửi lệnh "2,0" khi thả nút
                                                    Lenhref.child("Lenh").setValue("2,0")
                                                    true
                                                }
                                                else -> false
                                            }
                                        }
                                ) {}
                            }
                        }
                    }
                }
            }
        }
    }}
@Preview(showBackground = true, widthDp = 800, heightDp = 600)
@Composable
fun CarControlScreenPreview() {
    CarControlScreen(onNavigateBack = {})
}
