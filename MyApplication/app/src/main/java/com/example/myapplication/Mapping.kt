package com.example.myapplication

import android.util.Log
import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappingScreen(onNavigateBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
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
                title = { Text("Auto Mode", color = Color.White, fontSize = 28.sp) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onNavigateBack() // Quay lại màn hình trước đó
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_back), // Đảm bảo `ic_back` tồn tại
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, // Làm cho TopAppBar trong suốt
                    scrolledContainerColor = Color.Transparent
                )) // Không thay đổi màu khi cuộn            )
        },
        containerColor = Color.Transparent // Scaffold trong suốt

    ) { paddingValues ->
        paddingValues
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_4),
                contentDescription = "BG",
                modifier = Modifier.fillMaxSize().width(3000.dp).height(1700.dp),
                contentScale = ContentScale.FillBounds // Cắt ảnh để vừa khít màn hình



            )
            Box (
                modifier = Modifier.align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.7f)),
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
                    Text("Chế độ: Điều khiển tự động", color = Color.White, fontSize = 40.sp)


                    Text(
                        text = "Trạng thái xe",
                        color = Color.White,
                        fontSize = 40.sp
                    )

                    Text(
                        text = carCommand.value,
                        color = Color.LightGray,
                        fontSize = 40.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }



        }

    }
}
