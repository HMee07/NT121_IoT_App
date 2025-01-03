package com.example.myapplication

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageButton
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase
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

    // Gia tri toc do, den xe
    val speedValue = remember { mutableStateOf(75f) } // Giá trị mặc định của slider (0-100)
    val lightValue = remember { mutableStateOf(75f) } // Giá trị mặc định của slider (0-100)

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
                title = { Text("Trang chủ", color = Color.Black) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            controlRef.child("Lenh").setValue("0")
                            onNavigateBack()

                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_back),
                            contentDescription = "Back",
                            tint = Color.Unspecified
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues -> paddingValues

        Box(
            modifier = Modifier
                .fillMaxSize()

        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_2),
                contentDescription = "BG",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds // Cắt ảnh để vừa khít màn hình

            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Spacer để tạo khoảng trống ở trên cùng, đẩy toàn bộ nội dung xuống
                Spacer(modifier = Modifier.height(100.dp))

                // Hiển thị video stream hoặc thông báo log
                Box(
                    modifier = Modifier
                        .width(550.dp)
                        .height(400.dp)
                        .background(Color.Gray)
                ) {
                    imageBitmap.value?.let { bitmap ->
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Video Stream",
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: Text(
                        text = logMessage.value,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Spacer để tạo khoảng trống
                Spacer(modifier = Modifier.height(50.dp))

                // Slider tốc độ
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp)) // Bo góc slider
                        .background(Color.White.copy(alpha = 0.8f))
                ) {
                    // Icon tốc độ
                    Image(
                        painter = painterResource(id = R.drawable.icon_speed),
                        contentDescription = "Speed Icon",
                        modifier = Modifier.size(50.dp)
                    )
                    // Slider
                    Slider(
                        value = speedValue.value,
                        onValueChange = { newValue ->
                            speedValue.value = newValue
                            controlRef.child("Toc_Do").setValue(newValue.toInt())
                        },
                        valueRange = 0f..100f,
                        modifier = Modifier.width(500.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp)) // Tạo khoảng trống giữa Slider và Icon
                }
                // Spacer để tạo khoảng trống
                Spacer(modifier = Modifier.height(20.dp))
                // Slider đèn
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp)) // Bo góc slider
                        .background(Color.White.copy(alpha = 0.8f))
                ) {
                    // Icon đèn
                    Image(
                        painter = painterResource(id = R.drawable.light_icon),
                        contentDescription = "Light Icon",
                        modifier = Modifier.size(50.dp)
                    )
                    // Slider đèn
                    Slider(
                        value = lightValue.value,
                        onValueChange = { newValue ->
                            lightValue.value = newValue
                            controlRef.child("Den_Xe").setValue(newValue.toInt())
                        },
                        valueRange = 0f..100f,
                        modifier = Modifier
                            .width(500.dp)
                    )
                }
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

                    // Nút Kèn
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFF3BD) // Màu vàng
                        ),
                        modifier = Modifier.size(70.dp)
                            .pointerInteropFilter { motionEvent ->
                                when (motionEvent.action) {
                                    android.view.MotionEvent.ACTION_DOWN -> {
                                        // Gửi lệnh "3" khi nhấn giữ nút
                                        Lenhref.child("Ken_Xe").setValue("1,")
                                        true
                                    }

                                    android.view.MotionEvent.ACTION_UP -> {
                                        // Gửi lệnh "3,0" khi thả nút
                                        Lenhref.child("Ken_Xe").setValue("1,0")
                                        true
                                    }

                                    else -> false
                                }
                            }, // Đặt size để tạo hình tròn
                        shape = RoundedCornerShape(50) // Bo tròn toàn bộ nút
                    ) {

                        Image(
                            painter = painterResource(id = R.drawable.horn_icon),
                            contentDescription = "Kèn",
                            modifier = Modifier.size(70.dp).padding(0.dp).scale(2f)
                        )
                    }

                // Hàng chứa các nút điều khiển Trái/Phải
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // Nút Trái
                    Button(
                        onClick = {},
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent.copy(alpha = 0f)
                        ),
                        modifier = Modifier
                            .size(150.dp)
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
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.d_1),
                            contentDescription = "Trai",
                            modifier = Modifier.size(90.dp).scale(2f)
                        )
                    }

                    // Nút Phải
                    Button(
                        onClick = {},
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent.copy(alpha = 0f)
                        ),
                        modifier = Modifier
                            .size(150.dp)
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
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.r_1),
                            contentDescription = "Phai",
                            modifier = Modifier.size(90.dp).scale(2f)
                        )
                    }

                }
            }
            // Các nút điều khiển bên phải
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Nút chụp hình
                    Button(
                        onClick = {
                            controlRef.child("Chup_Hinh").setValue(1)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB0B0B0) // Màu vàng nhạt
                        ),
                        modifier = Modifier
                            .size(70.dp), // Đặt size chiều rộng và cao bằng nhau để hình tròn
                        shape = RoundedCornerShape(50) // Bo tròn 50 để tạo hình tròn
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.take_icon),
                            contentDescription = "Chụp",
                            modifier = Modifier.fillMaxSize().scale(3f).size(90.dp)
                        )
                    }

                    // Hàng chứa các nút điều khiển
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.align(Alignment.End) // Đẩy hàng nút sang cạnh phải
                    ) {
                        // Nút Stop
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black.copy(alpha = 0f)
                            ),
                            modifier = Modifier.size(150.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.stop_1),
                                contentDescription = "Dung",
                                modifier = Modifier.fillMaxSize().scale(1.5f).size(150.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Nút Lên
                            Button(
                                onClick = {},
                                shape = RoundedCornerShape(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black.copy(alpha = 0f)
                                ),
                                modifier = Modifier
                                    .size(150.dp)
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
                                    }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.l_1),
                                    contentDescription = "Tien",
                                    modifier = Modifier.size(90.dp).scale(2f)
                                )
                            }

                            // Nút Xuống
                            Button(
                                onClick = {},
                                shape = RoundedCornerShape(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray.copy(alpha = 0f)
                                ),
                                modifier = Modifier
                                    .size(150.dp)
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
                            )

                            {
                                Image(
                                    painter = painterResource(id = R.drawable.upl_1),
                                    contentDescription = "Lui",
                                    modifier = Modifier.size(90.dp).scale(2f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}
