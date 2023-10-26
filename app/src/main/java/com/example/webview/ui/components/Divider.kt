package com.example.webview.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DividerCompose(modifier: Modifier) {
    Row(modifier) {
        Divider(Modifier.fillMaxWidth().weight(0.8f))
    }
}