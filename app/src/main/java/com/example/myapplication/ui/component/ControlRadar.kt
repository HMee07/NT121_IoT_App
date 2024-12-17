package com.example.myapplication.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ControlRadar(
    selectedAngle: Int,
    onAngleChange: (Int) -> Unit,
    onReset: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Chọn góc quét cố định
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(60, 120, 240, 360).forEach { angle ->
                Button(
                    onClick = { onAngleChange(angle) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("$angle°")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tăng/Giảm góc
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { onAngleChange(selectedAngle - 1) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC2)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("-")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { onAngleChange(selectedAngle + 1) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC2)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hiển thị góc hiện tại
        Text(
            text = "Góc hiện tại: $selectedAngle°",
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Nút Reset
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Reset")
        }
    }
}
