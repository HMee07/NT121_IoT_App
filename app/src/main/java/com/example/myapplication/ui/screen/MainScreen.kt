package com.example.myapplication.ui.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.ui.component.DrawingArea
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

@Composable
fun MainScreen(navController: NavController) {
    val baseUrl = "http://172.20.10.2"
    val httpClient = remember { OkHttpClient() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun sendCommand(command: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val url = "$baseUrl/$command"
                val request = Request.Builder().url(url).build()
                httpClient.newCall(request).execute().close()
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


            DrawingArea(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .background(Color.White, RoundedCornerShape(8.dp))
            )
        }

        Button(
            onClick = { navController.navigate("radar") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Xem Radar")
        }
        Button(
            onClick = { navController.navigate("mapping") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Xem Map")
        }
        Button(
            onClick = { navController.navigate("setting") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Setting")
        }


    }
}
fun sendHttpCommand(command: String, esp32Ip: String) {
    val client = OkHttpClient()
    val url = "http://$esp32Ip/$command"

    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("HTTP", "Lỗi khi gửi lệnh: $command", e)
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                Log.d("HTTP", "Lệnh đã gửi thành công: $command")
            } else {
                Log.e("HTTP", "Lỗi phản hồi từ ESP32")
            }
        }




    })
}

@Preview(showBackground = true)
@Composable
fun PreviewScreen() {
    val mockNavController = rememberNavController() // Mock NavController
//    RadarScreen(navController = mockNavController)
//    MappingScreen(navController = mockNavController)
    MainScreen(navController = mockNavController)
//    SettingsScreen(navController = mockNavController)


}