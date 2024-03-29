package com.example.webview.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp


@Composable
fun ButtonCompose(onClick: () -> Unit, modifier: Modifier, label: String, enabled: Boolean) {
    Button(onClick = { onClick() }, modifier = modifier, enabled = enabled) {
        Text(label, fontSize = 12.sp)
    }
}