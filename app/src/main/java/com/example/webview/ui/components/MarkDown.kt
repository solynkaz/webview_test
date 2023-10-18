package com.example.webview.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.webview.readFileFromInternalStorage
import com.example.webview.viewmodel.MDEvent
import com.example.webview.viewmodel.MarkdownViewModel
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

//MarkDown Content
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkDownContent(
    buttonsVisibility: MutableState<Boolean>,
    currentFilePath: MutableState<String>,
    currentFileExtension: MutableState<String>,
    mdModel: MarkdownViewModel = hiltViewModel()
) {
    val mdState = mdModel.mdState
    val isLoading = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val firstLoad = remember { mutableStateOf(true) }
    val dirs = context.filesDir
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
    Scaffold(
        content = { padding ->
            ModalNavigationDrawer(
                drawerContent = {
                    ModalDrawerSheet {
                    }
                }
            ) {
                if (mdState.isLoading) {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
                RichText(
                    modifier = Modifier
                        .padding(padding)
                        .padding(all = 15.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Markdown(
                        content = mdState.data,
                        onLinkClicked = { link ->
                            currentFilePath.value = link
                            Log.i("MarkDown", "Link changed to ${currentFilePath.value}")
                            mdModel.onEvent(
                                MDEvent.loadFile(
                                    currentFileExtension = currentFileExtension,
                                    currentFilePath = currentFilePath,
                                    context = context,
                                )
                            )
                        })
                }
            }
        },
        modifier = Modifier
            .padding(1.dp)
            .fillMaxSize()
    )
}
