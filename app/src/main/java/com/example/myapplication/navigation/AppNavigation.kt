package com.example.myapplication.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screen.MainScreen
import com.example.myapplication.ui.screen.MappingScreen
import com.example.myapplication.ui.screen.RadarScreen
import com.example.myapplication.ui.screen.SettingsScreen
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val database = FirebaseDatabase.getInstance().reference

//    fun navigateIfAvailable(navDestination: String, mode: String) {
//        scope.launch(Dispatchers.IO) {
//            // Điều hướng tới nút tương ứng trong cấu trúc mới
//            val modePath = "Car_Support/Giao_Dien/$mode"
//
//            // Kiểm tra trạng thái của chế độ
//            val isAvailable = database.child(modePath).get().await().value.toString() == "false"
//            if (isAvailable) {
//                // Cập nhật trạng thái thành "true"
//                database.child(modePath).setValue("true").await()
//                navController.navigate(navDestination)
//            } else {
//                // Thông báo nếu chế độ đang được sử dụng
//                scope.launch(Dispatchers.Main) {
//                    Toast.makeText(navController.context, "Chế độ này đang được sử dụng!", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }

    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }

        composable("radar") { RadarScreen(navController) }
        composable("mapping") { MappingScreen(navController) }
        composable("setting") { SettingsScreen(navController) }


    }
}
