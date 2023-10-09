package com.example.webview

import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText

val MarkDownPage =             """
## Вопрос

Как изменить владельца файла или директории?

## Ответ

Владельца файла или директории можно изменить с помощью **[chown](https://ru.wikipedia.org/wiki/Chown)**. Общий вид команды:

```shell
chown [-cfhvR] [--dereference] [--reference=rfile] пользователь[:группа] файл
```

Например:

```shell
sudo chown -R admin:admin /opt/idea-IU-213.7172.25/*
```

Такой вызов сделает пользователя `admin` из группы `admin` владельцем всех (`-R`) файлов и директорий в директории `/opt/idea-IU-213.7172.25`
    """.trimIndent()


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Calling the composable function
            // to display element and its contents
            MainContent()
        }
    }
}

// Creating a composable
// function to display Top Bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent() {
    Scaffold(
        content = { MarkDownContent() }
    )
}

//MarkDown Content
@Composable
fun MarkDownContent() {
    RichText(
        modifier = Modifier.padding(16.dp)
    ) {
        Markdown(
            MarkDownPage
        )
    }
}
@Composable
fun WebViewContent(){

    // Declare a string that contains a url
//    val mUrl = "file:///android_asset/bz.html"
    val mUrl = "file:///android_asset/bd.md"
    AndroidView(factory = {
        WebView(it).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient = WebViewClient()
            loadUrl(mUrl)
        }
    }, update = {
        it.loadUrl(mUrl)
    })
}

// For displaying preview in
// the Android Studio IDE emulator
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainContent()
}
