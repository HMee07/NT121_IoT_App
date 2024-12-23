package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
fun lineTrackingScreen(onNavigateBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Line Tracking", color = Color.White, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    // Đặt giá trị Firebase về "false"
                                    FirebaseDatabase.getInstance()
                                        .getReference("Interface/lineTracking")
                                        .setValue("false").await()
                                    Log.d("LineTrackingScreen", "Successfully reset 'LineTracking' to false")
                                } catch (e: Exception) {
                                    Log.e("LineTrackingScreen", "Failed to reset 'LineTracking' to false", e)
                                }
                            }
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.DarkGray)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues) // Đảm bảo padding từ Scaffold
                .verticalScroll(scrollState), // Thêm khả năng cuộn
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Căn giữa theo chiều dọc
        ) {
            // Nội dung giao diện
            Text(
                text = "Line Tracking Mode",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp) // Khoảng cách với các thành phần khác
            )

            // Nút điều khiển
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            FirebaseDatabase.getInstance()
                                .getReference("Car_Control/lineTracking/start")
                                .setValue("true").await()
                            Log.d("LineTrackingScreen", "Started line tracking")
                        } catch (e: Exception) {
                            Log.e("LineTrackingScreen", "Failed to start line tracking", e)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                shape = CircleShape,
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Start Tracking", color = Color.Black)
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            FirebaseDatabase.getInstance()
                                .getReference("Car_Control/lineTracking/stop")
                                .setValue("true").await()
                            Log.d("LineTrackingScreen", "Stopped line tracking")
                        } catch (e: Exception) {
                            Log.e("LineTrackingScreen", "Failed to stop line tracking", e)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = CircleShape,
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Stop Tracking", color = Color.White)
            }
        }
    }
}
