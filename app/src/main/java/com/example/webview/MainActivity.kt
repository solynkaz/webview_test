package com.example.webview

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.webview.ui.components.ControlButtons
import com.example.webview.ui.components.MarkDownContent
import com.example.webview.viewmodel.GitRepoEvent
import com.example.webview.viewmodel.GitViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class MainActivity : ComponentActivity(
) {
    //TODO Где хранить настройки (урл репо)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            main_screen()
        }
    }
}

@Composable
fun main_screen(
    gitViewModel: GitViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val gitRepoState = gitViewModel.gitRepoState

    if (gitRepoState.gitRepoUrl == "") {
        gitViewModel.onEvent(GitRepoEvent.GetRepoUrl(AppConsts.WIKI_JS_BEARER, context))
    }

    if (isRepoEmpty("", context) && gitRepoState.isGitRepoUrlLoaded) {
        gitViewModel.onEvent(GitRepoEvent.GitRepoClone(gitRepoState.gitRepoUrl!!))
    }

    val coroutineScope = rememberCoroutineScope()
    val buttonsVisibility = remember { mutableStateOf(true) }
    val mdContent = remember { mutableStateOf("") }
    val currentFilePath = remember { mutableStateOf("/home") }

    val firstLaunch = remember { mutableStateOf(true) }
    val isThereNetworkConnection = remember { mutableStateOf(true) }
    if (firstLaunch.value || gitRepoState.isGitClonePending || gitRepoState.isGetRepoUrlPending) {
        CircularProgressIndicator()

        isThereNetworkConnection.value = isOnline(context)
        firstLaunch.value = false
        Log.i("Internet", "Internet connection is ${isThereNetworkConnection.value}")


    } else {
        if (!isThereNetworkConnection.value) {
            if (buttonsVisibility.value) {
                ControlButtons(currentFilePath, mdContent, buttonsVisibility, coroutineScope)
            } else {
                MarkDownContent(mdContent.value, buttonsVisibility, currentFilePath)
            }
        } else {
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


