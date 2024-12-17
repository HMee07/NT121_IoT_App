package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.ui.component.RadarView
import com.example.myapplication.ui.component.WarningDisplay

@Composable
fun RadarScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Màn hình Radar", color = Color.Black)
        Spacer(modifier = Modifier.height(16.dp))
        RadarView(modifier = Modifier.fillMaxWidth().height(300.dp))
        Spacer(modifier = Modifier.height(16.dp))
        WarningDisplay()
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Quay lại")
        }
    }
}
