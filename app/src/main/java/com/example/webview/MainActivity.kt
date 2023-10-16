package com.example.webview

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.webview.ui.components.ControlButtons
import com.example.webview.ui.components.MarkDownContent
import java.io.File


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val localContext = LocalContext.current
            val firstLaunch = remember { mutableStateOf(true) }
            val isThereNetworkConnection = remember { mutableStateOf(true) }

            if (firstLaunch.value) {
                isThereNetworkConnection.value = isOnline(localContext)
                firstLaunch.value = false
                Log.i("Internet", "Internet connection is ${isThereNetworkConnection.value}")
            }

            val coroutineScope = rememberCoroutineScope()
            val buttonsVisibility = remember { mutableStateOf(true) }
            val mdContent = remember { mutableStateOf("") }
            val currentFilePath = remember { mutableStateOf("/home") }
            if (!isThereNetworkConnection.value) {
                if (buttonsVisibility.value) {
                    ControlButtons(currentFilePath, mdContent, buttonsVisibility, coroutineScope)
                } else {
                    MarkDownContent(mdContent.value, buttonsVisibility, currentFilePath)
                }
            } else {
                val context = LocalContext.current

                val webView = remember {
                    WebView(context).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                    }
                }
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize(),
                    factory = { webView },
                    update = {
                        it.loadUrl(AppConsts.KB_URL)
                    })
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
