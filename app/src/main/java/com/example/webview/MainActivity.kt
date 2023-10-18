package com.example.webview

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.webview.ui.components.ControlButtons
import com.example.webview.ui.components.MarkDownContent
import com.example.webview.viewmodel.GitRepoEvent
import com.example.webview.viewmodel.GitRepoState
import com.example.webview.viewmodel.GitViewModel
import com.example.webview.viewmodel.MDState
import com.example.webview.viewmodel.MarkdownViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import java.io.File


@AndroidEntryPoint
class MainActivity : ComponentActivity(
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isThereNetworkConnection = remember { mutableStateOf(true) }
            val firstLaunch = remember { mutableStateOf(true) }
            val context = LocalContext.current
            val prefs: SharedPreferences = context.getSharedPreferences(
                "prefs",
                MODE_PRIVATE
            )
            Main_screen(
                isThereNetworkConnection = isThereNetworkConnection,
                context = context,
                firstLaunch = firstLaunch,
                prefs = prefs
            )
        }
    }
}


@Composable
fun Main_screen(
    isThereNetworkConnection: MutableState<Boolean>,
    context: Context,
    firstLaunch: MutableState<Boolean>,
    prefs: SharedPreferences,
    gitViewModel: GitViewModel = hiltViewModel()
) {
    val gitRepoState = gitViewModel.gitRepoState
    val buttonsVisibility = remember { mutableStateOf(true) }
    val currentFile = remember { mutableStateOf("home") }
    val currentFileExtension = remember { mutableStateOf("md") }

    if (firstLaunch.value) {
        isThereNetworkConnection.value = isOnline(context)
        gitViewModel.onEvent(GitRepoEvent.LoadSettings(prefs = prefs))
        Log.i("Internet", "Internet connection is ${isThereNetworkConnection.value}")
        firstLaunch.value = false
    }
    if (isThereNetworkConnection.value) {
        if (gitRepoState.isGitClonePending || gitRepoState.isGitClonePending) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    Modifier.align(
                        Alignment.Center
                    )
                )
            }
        }
        if (gitRepoState.gitRepoUrl == "" && !gitRepoState.isGetRepoUrlPending) {
            gitViewModel.onEvent(GitRepoEvent.GetRepoUrl(AppConsts.WIKI_JS_BEARER, context))
        }
        if (!gitRepoState.isGitCloneLoaded && !gitRepoState.isGitClonePending && gitRepoState.gitRepoUrl != "") {
            gitViewModel.onEvent(
                GitRepoEvent.GitRepoClone(
                    context,
                    "zma@sibdigital.net",
                    "J0tul8878q1e3"
                )
            )
        }
        WebViewWithConnection(context = context)
    } else {
//        ControlButtons(currentFilePath, fileContent, buttonsVisibility, coroutineScope)
        if (currentFileExtension.value == "md") {
            MarkDownContent(
                buttonsVisibility,
                currentFileExtension = currentFileExtension,
                currentFilePath = currentFile
            )
        } else if (currentFileExtension.value == "html") {
            WebViewWithoutConnection(context = context)
        }
    }
}

@Composable
fun WebViewWithConnection(context: Context) {
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

@Composable
fun WebViewWithoutConnection(context: Context) {
    val mdState: MDState = hiltViewModel<MarkdownViewModel>().mdState

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
            it.loadDataWithBaseURL(null, mdState.data, "text/html", "UTF-8", null)
        })
}


