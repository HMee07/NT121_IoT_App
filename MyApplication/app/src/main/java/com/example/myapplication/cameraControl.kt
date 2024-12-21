package com.example.myapplication

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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

@Composable
fun CarControlScreen(onNavigateBack: () -> Unit) {
    val imageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }
    val logMessage = remember { mutableStateOf("Initializing WebSocket...") } // Log trạng thái
    val sliderValue = remember { mutableStateOf(75f) } // Giá trị mặc định của slider (0-100)

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

    Box( modifier = Modifier
        .fillMaxSize()
        .background(Color.Black) )// Đặt nền thành màu đen)
        {
        // Hiển thị video stream hoặc thông báo log
        Box(
            modifier = Modifier
                .size(300.dp) // Kích thước Box cố định
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

        // Nút trở về
        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Text("Trở về")
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
                    onClick = { controlRef.child("Ken_Xe").setValue("true") },
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

                // Hàng chứa các nút điều khiển Trái/Phải
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // Nút Trái
                    Button(
                        onClick = { controlRef.child("Cotrol/3").setValue("true") },
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        modifier = Modifier
                            .width(70.dp)
                            .height(90.dp)
                    ) {}

                    // Nút Phải
                    Button(
                        onClick = { controlRef.child("Cotrol/4").setValue("true") },
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        modifier = Modifier
                            .width(70.dp)
                            .height(90.dp)
                    ) {}

                }
            }
            // Các nút điều khiển bên phải với Slider
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
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
                            value = sliderValue.value,
                            onValueChange = { newValue ->
                                sliderValue.value = newValue
                                controlRef.child("Den_Xe").setValue(newValue.toInt()) },
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
                            onClick = { controlRef.child("Cotrol/0").setValue("true") },
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDC2626)
                            ),
                            modifier = Modifier
                                .width(70.dp)
                                .height(90.dp)
                        ) {}

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Nút Lên
                            Button(
                                onClick = { controlRef.child("Cotrol/1").setValue("true") },
                                shape = RoundedCornerShape(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                modifier = Modifier
                                    .width(75.dp)
                                    .height(40.dp)
                            ) {}

                            // Nút Xuống
                            Button(
                                onClick = { controlRef.child("Cotrol/2").setValue("true") },
                                shape = RoundedCornerShape(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                modifier = Modifier
                                    .width(75.dp)
                                    .height(40.dp)
                            ) {}
                        }
                    }
                }
            }
        }
    }
}
