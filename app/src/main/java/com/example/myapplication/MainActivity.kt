package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("radar") { RadarScreen(navController) }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    val baseUrl = "http://172.20.10.2" // Replace with your ESP32's IP address
    val httpClient = remember { OkHttpClient() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun sendCommand(command: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val url = "$baseUrl/$command"
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                response.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_up),
                    contentDescription = "Forward",
                    modifier = Modifier
                        .size(150.dp)
                        .clickable {
                            sendCommand("forward")
                            Toast.makeText(context, "Xe Tiến", Toast.LENGTH_SHORT).show()
                        },
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(id = R.drawable.arrow_down),
                    contentDescription = "Backward",
                    modifier = Modifier
                        .size(150.dp)
                        .clickable {
                            sendCommand("backward")
                            Toast.makeText(context, "Xe Lùi", Toast.LENGTH_SHORT).show()
                        },
                    contentScale = ContentScale.Fit
                )
            }

            DrawingArea(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .padding(8.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_left),
                    contentDescription = "Left",
                    modifier = Modifier
                        .size(150.dp)
                        .clickable {
                            sendCommand("left")
                            Toast.makeText(context, "Xe Rẽ Trái", Toast.LENGTH_SHORT).show()
                        },
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = "Right",
                    modifier = Modifier
                        .size(150.dp)
                        .clickable {
                            sendCommand("right")
                            Toast.makeText(context, "Xe Rẽ Phải", Toast.LENGTH_SHORT).show()
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("radar") },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text("Xem Radar")
        }
    }
}

@Composable
fun RadarScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Màn Hình Radar",
            fontSize = 18.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        RadarView(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(4.dp)
            .background(Color.LightGray))

        Spacer(modifier = Modifier.height(16.dp))

        WarningDisplay()

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Quay Lại")
        }
    }
}

@Composable
fun DrawingArea(modifier: Modifier) {
    Canvas(modifier = modifier) {
        // Placeholder for drawing area
    }
}

@Composable
fun RadarView(modifier: Modifier) {
    Canvas(modifier = modifier) {
        // Placeholder for radar visualization
    }
}

@Composable
fun WarningDisplay() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "Cảnh Báo: Vật thể ở khoảng cách nguy hiểm!",
            fontSize = 16.sp,
            color = Color.Red
        )
    }
}
