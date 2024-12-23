package com.example.myapplication

import MappingScreen
import com.example.myapplication.ui.screen.autoModeScreen
import com.example.myapplication.ui.screen.lineTrackingScreen
import com.example.myapplication.ui.screen.remoteScreen
import androidx.compose.runtime.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import com.google.accompanist.pager.*
import androidx.compose.ui.text.TextStyle

// Firebase references
val database = FirebaseDatabase.getInstance()
val GDRef = database.getReference("Interface")
val controlRef = database.getReference("Car_Control")

sealed class Screen {
    object Menu : Screen()
    object Radar : Screen()
    object DrawLine : Screen()
    object CarControl : Screen()
    object ObstacleAvoidance : Screen()
    object RemoteControl : Screen()
    object LineTracking : Screen()
    object Mapping : Screen()
}

class MainActivity : ComponentActivity() {
    private lateinit var GDRef: DatabaseReference
    private lateinit var controlRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Khởi tạo Firebase

        val database = FirebaseDatabase.getInstance()
        GDRef = database.getReference("Interface")
        controlRef = database.getReference("Car_Control")

        setContent {
            MyApplicationTheme {
                AppNavigator(GDRef, controlRef)
            }
        }
    }
}

@Composable
fun AppNavigator(GDRef: DatabaseReference, controlRef: DatabaseReference) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }

    val updateInterfaceState: (String, Boolean) -> Unit = { screenName, isActive ->
        GDRef.child(screenName).setValue(if (isActive) "true" else "false")
    }

    val navigateBack: (String) -> Unit = { screenName ->
        updateInterfaceState(screenName, false)
        currentScreen = Screen.Menu
    }

    when (currentScreen) {
        is Screen.Menu -> MainMenu(onNavigate = { screen ->
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
                updateInterfaceState(screenName, true)
            }
            currentScreen = screen
        })
        is Screen.Radar -> RadarScreen(onNavigateBack = { navigateBack("radarControl") })
        is Screen.CarControl -> CarControlScreen(onNavigateBack = { navigateBack("cameraControl") })
        Screen.DrawLine -> drawLineScreen(onNavigateBack = { navigateBack("drawLine") })
        Screen.LineTracking -> lineTrackingScreen(onNavigateBack = { navigateBack("scanLine") })
        Screen.Mapping -> MappingScreen(onNavigateBack = { navigateBack("Mapping") })
        Screen.ObstacleAvoidance -> autoModeScreen(onNavigateBack = { navigateBack("avoidObject") })
        Screen.RemoteControl -> remoteScreen(onNavigateBack = { navigateBack("remoteControl") })
    }
}

@Composable
fun MainMenu(onNavigate: (Screen) -> Unit) {
    val pagerState = rememberPagerState(initialPage = 0)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_3),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "MENU CHÍNH",
                color = Color.White,
                fontSize = 40.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Gray.copy(alpha = 0.5f),
                        offset = Offset(4f, 4f),
                        blurRadius = 6f
                    ),
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF30CFD0), Color(0xFF330867))
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalPager(
                count = 7, // Số lượng trang
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MenuOption(
                        imageRes = when (page) {
                            0 -> R.drawable.radar_icon
                            1 -> R.drawable.car_control
                            2 -> R.drawable.draw_line
                            3 -> R.drawable.avoid_icon
                            4 -> R.drawable.remote_control
                            5 -> R.drawable.line_tracking
                            else -> R.drawable.mapping
                        },
                        label = when (page) {
                            0 -> "Giao diện Radar"
                            1 -> "Điều khiển xe và camera"
                            2 -> "Vẽ đường đi"
                            3 -> "Auto Mode"
                            4 -> "Remote Control"
                            5 -> "Dò line"
                            else -> "Mapping"
                        },
                        onClick = {
                            onNavigate(
                                when (page) {
                                    0 -> Screen.Radar
                                    1 -> Screen.CarControl
                                    2 -> Screen.DrawLine
                                    3 -> Screen.ObstacleAvoidance
                                    4 -> Screen.RemoteControl
                                    5 -> Screen.LineTracking
                                    else -> Screen.Mapping
                                }
                            )
                        },
                        isSelected = pagerState.currentPage == page
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalPagerIndicator(
                pagerState = pagerState,
                activeColor = Color.White,
                inactiveColor = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun MenuOption(
    @DrawableRes imageRes: Int,
    label: String,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    Card(
        modifier = Modifier
            .size(300.dp)
            .clickable(onClick = onClick)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF8BA9C7) else Color.DarkGray
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = label,
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
