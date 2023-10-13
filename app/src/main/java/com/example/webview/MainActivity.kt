package com.example.webview

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.webview.components.ControlButtons
import com.example.webview.components.MarkDownContent
import java.io.File


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val firstLaunch = remember { mutableStateOf(true) }
            val isThereNetworkConnection = remember { mutableStateOf(true) }
            val coroutineScope = rememberCoroutineScope()
            val buttonsVisibility = remember { mutableStateOf(true) }
            val mdContent = remember { mutableStateOf("") }
            val currentFilePath = remember { mutableStateOf("/home") }
            if (isThereNetworkConnection.value) {
                if (buttonsVisibility.value) {
                    ControlButtons(currentFilePath, mdContent, buttonsVisibility, coroutineScope)
                } else {
                    MarkDownContent(mdContent.value, buttonsVisibility, currentFilePath)
                }
            } else {
                // TODO WebView
            }
        }
    }
}

fun readFileFromInternalStorage(path: String, context: Context): String {
    val filePath = "${context.filesDir}/${AppConsts.KB_FOLDER}${
        path.split("/").dropLast(1).joinToString(separator = "/")
    }";
    val fileName = "${path.split("/").last()}.md"
    Log.i("MarkDown", "Reading from $filePath")
    return File("$filePath/$fileName").bufferedReader().use { it.readText() }.trim()
}

