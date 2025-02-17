package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(onNavigateBack: () -> Unit) {

    var carCommand = remember { mutableStateOf("Đang dừng") } // Trạng thái lệnh (mặc định)

    // Lắng nghe lệnh từ Firebase
    LaunchedEffect(Unit) {
        val carControlRef = FirebaseDatabase.getInstance().getReference("Car_Control/Lenh")
        carControlRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val command = snapshot.getValue(String::class.java) // Lấy giá trị kiểu String
                carCommand.value = when (command) {
                    "1,","1,0" -> "Tiến"
                    "2,","2,0" -> "Lùi"
                    "3,","3,0" -> "Rẽ trái"
                    "4,","4,0" -> "Rẽ phải"
                    "0," -> "Đang dừng"
                    else -> "Không xác định"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RemoteScreen", "Lỗi khi đọc dữ liệu Firebase: ${error.message}")
            }
        })
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Remote Control", fontSize = 28.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(
                        onClick = {
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues -> paddingValues
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_4),
                contentDescription = "BG",

                        modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds // Cắt ảnh để vừa khít màn hình

            )
            Box (
                modifier = Modifier.align(Alignment.Center).fillMaxSize()
                    .background(Color.Black.copy(alpha = 1f)),
                contentAlignment = Alignment.Center // Căn giữa toàn bộ nội dung
            ){
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
//                    .background(color.)
                        .padding(16.dp),

                    horizontalAlignment = Alignment.CenterHorizontally,

                    verticalArrangement = Arrangement.Center
                ) {

                    Text("Chế độ: Điều khiển xe bằng remote", color = Color.White, fontSize = 40.sp)

                    Spacer(modifier = Modifier.width(24.dp) )

                    Image(
                        painter = painterResource(id = R.drawable.icon_remotehn),
                        contentDescription = "Remote Icon",
                        modifier= Modifier.scale(0.7f)


                    )

                    Text(
                        text = "Dùng Các Phím Trên Remote Để Điều Khiển Xe Di Chuyển",
                        color = Color.White,
                        fontSize = 40.sp

                    )


                }
            }

        }
    }
}
