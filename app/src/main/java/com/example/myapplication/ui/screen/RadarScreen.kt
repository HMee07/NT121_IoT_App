package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.ui.component.ControlRadar
import com.example.myapplication.ui.component.RadarView
import com.example.myapplication.ui.component.WarningDisplay

@Composable
fun RadarScreen(navController: androidx.navigation.NavController) {
    var selectedAngle = remember { mutableStateOf(60) } // Góc quét mặc định
    val objectCount = 8 // Giả lập số vật thể

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Radar (Bên trái)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.Black)
        ) {
            RadarView(modifier = Modifier.fillMaxSize()) // Radar View
        }

        // Control Buttons (Bên phải)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Có $objectCount vật thể đang ở xung quanh bạn!",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )

            // Gọi ControlRadar
            ControlRadar(
                selectedAngle = selectedAngle.value,
                onAngleChange = { newAngle -> selectedAngle.value = newAngle },
                onReset = { selectedAngle.value = 60 }
            )

            Button(onClick = { navController.popBackStack() }) {
                Text("Quay lại")
            }
        }
    }
}