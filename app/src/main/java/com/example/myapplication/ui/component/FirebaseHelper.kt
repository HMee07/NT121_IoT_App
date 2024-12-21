package com.example.myapplication.ui.component

import android.util.Log
import com.google.firebase.database.DatabaseReference


fun updateButtonState(button: Int, database: DatabaseReference) {
    val commands = listOf("0", "1", "2", "3", "4") // Các mã lệnh cho Dừng, Tiến, Lùi, Trái, Phải
    val updates = mutableMapOf<String, Any>()

    // Đặt trạng thái của nút được nhấn là "true", các nút khác là "false"
    commands.forEach {
        updates[it] = if (it == button.toString()) "true" else "false"
    }

    // Cập nhật trạng thái vào Firebase
    database.child("Car_Control/Control").updateChildren(updates).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("Firebase", "Trạng thái các nút đã được cập nhật (dưới dạng chuỗi)")
        } else {
            Log.e("Firebase", "Lỗi khi cập nhật trạng thái: ${task.exception}")
        }
    }
}
