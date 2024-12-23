package com.example.myapplication

import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
//import androidx.compose.foundation.pager.HorizontalPagerIndicator
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.Alignment
import com.example.myapplication.ui.screen.MappingScreen

// Biến toàn cục dùng để up lên firebase
val database = FirebaseDatabase.getInstance()
val GDRef = database.getReference("Interface")
val controlRef = database.getReference("Car_Control")
val Lenhref = database.getReference("Car_Control")

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
        is Screen.DrawLine -> drawLineScreen (onNavigateBack = { navigateBack("drawLine") })
       is Screen.LineTracking -> lineTrackingScreen (onNavigateBack = { navigateBack("scanLine") })
       is Screen.Mapping -> MappingScreen (onNavigateBack = { navigateBack("Mapping") })
       is  Screen.ObstacleAvoidance -> autoModeScreen (onNavigateBack = { navigateBack("avoidObject") })
        is Screen.RemoteControl -> RemoteScreen (onNavigateBack = { navigateBack("remoteControl") })
    }
}

@Suppress("DEPRECATION")
@Composable
fun MainMenu(onNavigate: (Screen) -> Unit) {
    val scrollState = rememberScrollState()

    val pageCount = 6 // Số trang

    val pagerState = rememberPagerState(
        initialPage = 0, // Trang ban đầu
        pageCount = { 6 } // Số lượng trang
    )
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(scrollState), // Thêm khả năng cuộn

        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.spacedBy(12.dp)
        verticalArrangement = Arrangement.Center // Căn giữa theo chiều dọc

    ) {
        Text(
            "Menu Chính",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp) // Thêm khoảng cách với các thành phần bên dưới

        )
        //che do chon
        // HorizontalPager để chọn chế độ
        // HorizontalPager để chọn chế độ
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp), // Tăng chiều cao để làm cho phần tử ở giữa nổi bật hơn
            contentPadding = PaddingValues(horizontal = 32.dp),

        ) { page ->
            // Căn giữa từng phần tử trong HorizontalPager
            Box(
                modifier = Modifier
                    .fillMaxSize(), // Bố cục ngang bằng toàn bộ trang
                contentAlignment = Alignment.Center // Căn giữa nội dung trong mỗi trang
            ) {
            MenuOption(
                imageRes = when (page) {
                    0 -> R.drawable.radar_icon
                    1 -> R.drawable.car_control
                    2 -> R.drawable.draw_line
                    3 -> R.drawable.avoid_icon
                    4 -> R.drawable.remote_control



                    else -> R.drawable.line_tracking
                },
                label = when (page) {
                    0 -> "Giao diện Radar"
                    1 -> "Điều khiển xe và camera"
                    2 -> "Vẽ đường đi"
                    3 -> "Auto Mode"
                    4 -> "Remote Control"



                    else -> "Dò line"
                },
                onClick = {
                    onNavigate(
                        when (page) {
                            0 -> Screen.Radar
                            1 -> Screen.CarControl
                            2 -> Screen.DrawLine
                            3 -> Screen.ObstacleAvoidance
                            4 -> Screen.RemoteControl


                            else -> Screen.LineTracking
                        }
                    )
                },
                isSelected = pagerState.currentPage == page // Xác định trạng thái chọn
            )
                }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nút điều hướng (Trái/Phải)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            (pagerState.currentPage - 1).coerceAtLeast(0)
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Trái", color = Color.White)
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            (pagerState.currentPage + 1).coerceAtMost(pageCount - 1)
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Phải", color = Color.White)
            }
        }

        // Hiển thị chỉ số trang hiện tại
        HorizontalPagerIndicator(
            pagerState = pagerState,
            activeColor = Color.White,
            inactiveColor = Color.Gray,
            modifier = Modifier
                .align(
                    Alignment.CenterHorizontally

                )
        )
    }
}
    @Composable
    fun HorizontalPagerIndicator(
        pagerState: PagerState,
        activeColor: Color,
        inactiveColor: Color,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center
        ) {
            for (i in 0 until pagerState.pageCount) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(8.dp)
                        .background(
                            color = if (pagerState.currentPage == i) activeColor else inactiveColor,
                            shape = RoundedCornerShape(50)
                        )
                )
            }
        }
    }




//        // Các item menu với hình ảnh minh họa
//        MenuItem(imageRes = R.drawable.radar_icon, label = "Giao diện Radar") {
//            onNavigate(Screen.Radar)
//        }
//        MenuItem(imageRes = R.drawable.car_control, label = "Điều khiển xe và camera") {
//            onNavigate(Screen.CarControl)
//        }
//        MenuItem(imageRes = R.drawable.draw_line, label = "Vẽ đường đi") {
//            onNavigate(Screen.DrawLine)
//        }
//        MenuItem(imageRes = R.drawable.avoid_icon, label = "Chạy xe tránh vật cản") {
//            onNavigate(Screen.ObstacleAvoidance)
//        }
//        MenuItem(imageRes = R.drawable.remote_control, label = "Điều khiển xe bằng remote") {
//            onNavigate(Screen.RemoteControl)
//        }
//        MenuItem(imageRes = R.drawable.line_tracking, label = "Dò line") {
//            onNavigate(Screen.LineTracking)
//        }
//        MenuItem(imageRes = R.drawable.screenshot_icon, label = "Dò map") {
//            onNavigate(Screen.Mapping)
//        }



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
    @Composable
    fun MenuOption(
        @DrawableRes imageRes: Int,
        label: String,
        onClick: () -> Unit,
        isSelected: Boolean = false // Trạng thái chọn
    ) {
        val elevation = if (isSelected) 12.dp else 6.dp // Tăng độ sâu khi được chọn
        val borderColor = if (isSelected) Color.Cyan else Color.Transparent // Thay đổi màu viền khi chọn

        Card(
            modifier = Modifier
                .size(400.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(elevation),
            border = BorderStroke(2.dp, borderColor), // Viền nổi bật khi chọn
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
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
                    modifier = Modifier.size(150.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
