package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun autoModeScreen(onNavigateBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto Mode", color = Color.White, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onNavigateBack() // Quay lại màn hình trước đó
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back), // Đảm bảo `ic_back` tồn tại
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


            )
            // Nội dung cuộn
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState) // Cho phép cuộn nội dung
                    .padding(16.dp), // Padding cho nội dung
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Văn bản "Auto Mode" trên ảnh nền
                Text(
                    text = "Auto Mode",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 48.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Thêm các thành phần nội dung khác ở đây nếu cần
                Text(
                    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                    color = Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }


        }

    }
}
