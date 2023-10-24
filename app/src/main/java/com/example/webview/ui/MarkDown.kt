package com.example.webview.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.webview.viewmodel.MDEvent
import com.example.webview.viewmodel.MarkdownViewModel
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText

//MarkDown Content
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkDownContent(
    currentFilePath: MutableState<String>,
    currentFileExtension: MutableState<String>,
    mdModel: MarkdownViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val firstLoad = remember { mutableStateOf(true) }
    val scrollState = remember { mutableStateOf(ScrollState(0)) }

    if (firstLoad.value) {
        mdModel.onEvent(
            MDEvent.loadFile(
                currentFileExtension = currentFileExtension,
                currentFilePath = currentFilePath,
                context = context,
            )
        )
        firstLoad.value = false
    }
    RichText(
        modifier = Modifier
            .padding()
            .padding(all = 15.dp)
            .verticalScroll(scrollState.value)
    ) {
        Markdown(
            content = mdModel.mdState.data,
            onLinkClicked = { link ->
                if (link.startsWith("http")) {
                    Toast.makeText(context, "Ссылка недоступна.", Toast.LENGTH_SHORT).show()
                } else {
                    scrollState.value = ScrollState(0)
                    currentFilePath.value = link
                    Log.i("MarkDown", "Link changed to ${currentFilePath.value}")
                    mdModel.onEvent(
                        MDEvent.loadFile(
                            currentFileExtension = currentFileExtension,
                            currentFilePath = currentFilePath,
                            context = context,
                        )
                    )
                }
            })
    }
}
