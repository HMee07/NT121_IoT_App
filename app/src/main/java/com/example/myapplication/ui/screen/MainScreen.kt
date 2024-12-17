package com.example.myapplication.ui.screen

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.ui.component.DrawingArea
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

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
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_up),
                    contentDescription = "Tiến",
                    modifier = Modifier
                        .size(100.dp)
                        .clickable {
                            sendCommand("forward")
                            Toast.makeText(context, "Xe Tiến", Toast.LENGTH_SHORT).show()
                        },
                    contentScale = ContentScale.Fit
                )
            }

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
    }
}
