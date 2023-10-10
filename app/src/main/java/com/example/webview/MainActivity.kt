package com.example.webview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Calling the composable function
            // to display element and its contents
            TopAppBar(title = { "Content" })
            MarkDownContent()
        }
    }
}

//MarkDown Content
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkDownContent() {
    val context = LocalContext.current
    val currentFilePath = remember { mutableStateOf("home.md")}
    val parsedMdFile = remember { mutableStateOf(parseMdFile(currentFilePath.value, context)) }
    Scaffold(
        content = {
                padding ->
            RichText(
                modifier = Modifier
                    .padding(padding)
                    .padding(all = 15.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Markdown(
                    content = parsedMdFile.value,
                    onLinkClicked = { link ->
                        currentFilePath.value = link
                        parsedMdFile.value = parseMdFile(currentFilePath.value, context)
                    })
            }
        },
        ///общие-принципы/wiki/Синтаксис-Markdown
        modifier = Modifier
            .padding(1.dp)
    )
}
fun parseMdFile(path: String, context: Context): String {
    return context.assets.open("assets$path").bufferedReader().use { it.readText() }.trim()
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContent() {
//    val url by remember { mutableStateOf("https://kb.sibdigital.net/") }
//    val url by remember { mutableStateOf("file:///android_asset/knowledgebase.html") }
    val url by remember { mutableStateOf("file:///android_asset/home.md") }
//    val url by remember { mutableStateOf("file:///android_asset/bz.html") }q
    AndroidView(factory = {
        WebView(it)
            .apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                loadUrl(url)
        }
    }, update = {
        it.loadUrl(url)
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun prev() {
    val context = LocalContext.current
    Scaffold(
        content = { paddingValues ->
            Button(onClick = { Toast.makeText(context, "Text", Toast.LENGTH_SHORT) }) {
                Text("Filled")
            }
        }
    )
}