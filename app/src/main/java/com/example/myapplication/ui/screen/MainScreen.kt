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
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    val database = FirebaseDatabase.getInstance().reference

    val context = LocalContext.current
//    fun navigateWithCheck(mode: Int, navDestination: String) {
//        scope.launch(Dispatchers.IO) {
//            val isAvailable = database.child("Code_Giao_Dien").child(mode.toString()).get().await().value.toString() == "false"
//            if (isAvailable) {
//                database.child("Code_Giao_Dien").child(mode.toString()).setValue("true").await()
//                scope.launch(Dispatchers.Main) { navController.navigate(navDestination) }
//            } else {
//                scope.launch(Dispatchers.Main) {
//                    Toast.makeText(navController.context, "Chế độ này đang được sử dụng!", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
// Hàm kiểm tra chế độ và điều hướng
fun navigateWithCheck(mode: String, navDestination: String) {
    scope.launch(Dispatchers.IO) {
        val modePath = "Car_Support/Giao_Dien/$mode" // Đường dẫn tới chế độ
        try {
            // Kiểm tra trạng thái chế độ
            val isAvailable = database.child(modePath).get().await().value.toString() == "false"
            if (isAvailable) {
                // Cập nhật trạng thái thành "true"
                database.child(modePath).setValue("true").await()
                scope.launch(Dispatchers.Main) { navController.navigate(navDestination) }
            } else {
                // Hiển thị thông báo nếu chế độ đang được sử dụng
                scope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Chế độ này đang được sử dụng!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Lỗi khi kiểm tra chế độ: $e")
        }
    }
}

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

        Button(onClick = { navigateWithCheck("Tranh_Vat", "radar") }, modifier = Modifier.fillMaxWidth()) {
            Text("Chế độ Radar")
        }
        Button(onClick = { navigateWithCheck("Mapping", "mapping") }, modifier = Modifier.fillMaxWidth()) {
            Text("Chế độ Mapping")
        }
        Button(onClick = { navigateWithCheck("Remote", "setting") }, modifier = Modifier.fillMaxWidth()) {
            Text("Chế độ Setting")
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