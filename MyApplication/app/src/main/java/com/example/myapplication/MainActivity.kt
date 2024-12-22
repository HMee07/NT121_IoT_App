package com.example.myapplication

import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource

// Biến toàn cục dùng để up lên firebase
val database = FirebaseDatabase.getInstance()
val GDRef = database.getReference("Interface")
val controlRef = database.getReference("Car_Control")

sealed class Screen {
    object Menu : Screen()
    object Radar : Screen()
    object DrawLine : Screen ()
    object CarControl : Screen()
    object ObstacleAvoidance : Screen()
    object RemoteControl : Screen()
    object LineTracking : Screen()
    object Mapping : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                AppNavigator()
            }
        }
    }
}

@Composable
fun AppNavigator() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }
    // Hàm cập nhật trạng thái giao diện trên Firebase
    val updateInterfaceState: (String, Boolean) -> Unit = { screenName, isActive ->
        GDRef.child(screenName).setValue(if (isActive) "true" else "false")
    }
    // Hàm quay lại Menu
    val navigateBack: (String) -> Unit = { screenName ->
        updateInterfaceState(screenName, false) // Trả về false khi thoát giao diện
        currentScreen = Screen.Menu
    }
    when (currentScreen) {
        is Screen.Menu -> MainMenu(onNavigate = { screen ->
            // Đặt giao diện hiện tại thành true khi vào
            val screenName = when (screen) {
                is Screen.Radar -> "radarControl"
                is Screen.CarControl -> "cameraControl"
                is Screen.DrawLine -> "drawLine"
                is Screen.LineTracking -> "scanLine"
                is Screen.Mapping -> "Mapping"
                is Screen.ObstacleAvoidance -> "avoidObject"
                is Screen.RemoteControl -> "remoteControl"
                else -> ""
            }
            if (screenName.isNotEmpty()) {
                updateInterfaceState(screenName, true) // Cập nhật trạng thái giao diện thành true
            }
            currentScreen = screen
        })
        is Screen.Radar -> RadarScreen(onNavigateBack = { navigateBack("radarControl") })
        is Screen.CarControl -> CarControlScreen(onNavigateBack = { navigateBack("cameraControl") })
        Screen.DrawLine -> TODO()
        Screen.LineTracking -> TODO()
        Screen.Mapping -> TODO()
        Screen.ObstacleAvoidance -> TODO()
        Screen.RemoteControl -> TODO()
    }
}

@Composable
fun MainMenu(onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Menu Chính",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        // Các item menu với hình ảnh minh họa
        MenuItem(imageRes = R.drawable.radar_icon, label = "Giao diện Radar") {
            onNavigate(Screen.Radar)
        }
        MenuItem(imageRes = R.drawable.car_control, label = "Điều khiển xe và camera") {
            onNavigate(Screen.CarControl)
        }
        MenuItem(imageRes = R.drawable.draw_line, label = "Vẽ đường đi") {
            onNavigate(Screen.DrawLine)
        }
        MenuItem(imageRes = R.drawable.avoid_icon, label = "Chạy xe tránh vật cản") {
            onNavigate(Screen.ObstacleAvoidance)
        }
        MenuItem(imageRes = R.drawable.remote_control, label = "Điều khiển xe bằng remote") {
            onNavigate(Screen.RemoteControl)
        }
        MenuItem(imageRes = R.drawable.line_tracking, label = "Dò line") {
            onNavigate(Screen.LineTracking)
        }
        MenuItem(imageRes = R.drawable.screenshot_icon, label = "Dò map") {
            onNavigate(Screen.Mapping)
        }
    }
}

@Composable
fun MenuItem(@DrawableRes imageRes: Int, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick)
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = label,
                modifier = Modifier
                    .size(60.dp)
                    .padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}



