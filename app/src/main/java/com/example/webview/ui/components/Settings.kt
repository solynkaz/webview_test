package com.example.webview.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun Settings_Screen() {
    Column() {
        for (i in 0..10) {
            Button(onClick = { /*TODO*/ }) {
                Text("$i")
            }
        }
    }
}