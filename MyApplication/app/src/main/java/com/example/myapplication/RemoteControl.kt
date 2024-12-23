package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


data class RemoteControl(val KhoangCach: Int = 0, val Goc: Int = 0)

fun calculate_Object_Width(objectData: List<RemoteControl>): Float {
    if (objectData.size < 2) return 0f
    val angleStart = Math.toRadians(objectData.first().Goc.toDouble())
    val angleEnd = Math.toRadians(objectData.last().Goc.toDouble())
    val rStart = objectData.first().KhoangCach.toFloat()
    val rEnd = objectData.last().KhoangCach.toFloat()

    val x1 = rStart * kotlin.math.cos(angleStart).toFloat()
    val y1 = rStart * kotlin.math.sin(angleStart).toFloat()
    val x2 = rEnd * kotlin.math.cos(angleEnd).toFloat()
    val y2 = rEnd * kotlin.math.sin(angleEnd).toFloat()

    return kotlin.math.sqrt((x2 - x1).let { it * it } + (y2 - y1).let { it * it })

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(onNavigateBack: () -> Unit) {


    val coroutineScope = rememberCoroutineScope()



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Remote Control", color = Color.White) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues -> paddingValues
         Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                ) {
            Image(
                painter = painterResource(id = R.drawable.bg_4),
                contentDescription = "BG",
                modifier = Modifier.fillMaxSize()
            )

        }
    }
}
