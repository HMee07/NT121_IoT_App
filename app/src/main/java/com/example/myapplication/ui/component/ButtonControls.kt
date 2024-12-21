package com.example.myapplication.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase

@Composable
fun ButtonControls(navController: NavController, isFullScreen: MutableState<Boolean>) {
    val database = FirebaseDatabase.getInstance().reference.child("Code_Number")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { sendCommandToFirebase(0, database) }) {
                Text("Dừng")
            }
            Button(onClick = { sendCommandToFirebase(1, database) }) {
                Text("Tiến")
            }
        }

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { sendCommandToFirebase(3, database) }) {
                Text("Trái")
            }
            Button(onClick = { sendCommandToFirebase(4, database) }) {
                Text("Phải")
            }
        }

        Button(onClick = { sendCommandToFirebase(2, database) }) {
            Text("Lùi")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { isFullScreen.value = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("Mở rộng radar", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("main") }) {
            Text("Quay lại")
        }
    }
}

// Hàm gửi lệnh Firebase
fun sendCommandToFirebase(command: Int, database: com.google.firebase.database.DatabaseReference) {
    database.child(command.toString()).setValue(true)
}
