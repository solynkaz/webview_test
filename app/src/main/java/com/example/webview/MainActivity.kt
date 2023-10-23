package com.example.webview

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebBackForwardList
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.webview.controller.isOnline
import com.example.webview.ui.components.LoginCompose
import com.example.webview.ui.components.MarkDownContent
import com.example.webview.ui.components.Settings_Screen
import com.example.webview.viewmodel.AppEvent
import com.example.webview.viewmodel.AppViewModel
import com.example.webview.viewmodel.GitRepoEvent
import com.example.webview.viewmodel.GitViewModel
import com.example.webview.viewmodel.MDState
import com.example.webview.viewmodel.MarkdownViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity(
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            val isThereNetworkConnection = remember { mutableStateOf(isOnline(context = context)) }
            val firstLaunch = remember { mutableStateOf(true) }
            val prefs: SharedPreferences = context.getSharedPreferences(
                "prefs",
                MODE_PRIVATE
            )
            NavHost(navController = navController, startDestination = "Main_Screen") {
                composable("Main_Screen") {
                    Main_Screen(
                        isThereNetworkConnection = isThereNetworkConnection,
                        context = context,
                        firstLaunch = firstLaunch,
                        prefs = prefs,
                        onNavToSettings = { navController.navigate("Settings_Screen") }
                    )
                    BackHandler(true) {
                    }
                }
                composable("Settings_Screen") {
                    Settings_Screen()
                    BackHandler(true) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}


@Composable
fun Main_Screen(
    isThereNetworkConnection: MutableState<Boolean>,
    context: Context,
    firstLaunch: MutableState<Boolean>,
    prefs: SharedPreferences,
    gitViewModel: GitViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onNavToSettings: () -> Unit = {}
) {
    val currentFile = remember { mutableStateOf("home") }
    val currentFileExtension = remember { mutableStateOf("md") }

    FloatingActionButton(onClick = { onNavToSettings() }) { Text("Chto") }

    if (firstLaunch.value) {
        gitViewModel.onEvent(GitRepoEvent.LoadGitSettings(prefs = prefs))
        appViewModel.onEvent(AppEvent.LoadAppSettings(prefs = prefs))
        firstLaunch.value = false
    }
    if (isThereNetworkConnection.value) {
        WebViewCompose(context = context, type = "web")
    } else {
        if (currentFileExtension.value == "md") {
            MarkDownContent(
                currentFileExtension = currentFileExtension,
                currentFilePath = currentFile
            )
        } else if (currentFileExtension.value == "html") {
            WebViewCompose(context = context, type = "html")
        }
    }
}

@Composable
fun WebViewCompose(context: Context, type: String) {
    val mdModel: MarkdownViewModel = hiltViewModel()
    val data = remember { mutableStateOf(mdModel.mdState.data) }
    val webView = remember {
        WebView(context).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.useWideViewPort = true
            settings.domStorageEnabled = true
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    Log.d("WebViewDebug", "JavaScript Console: ${consoleMessage.message()}")
                    return true
                }
            }
        }
    }

    val webHistory: WebBackForwardList? by rememberUpdatedState(newValue = webView.copyBackForwardList())
    AndroidView(
        modifier = Modifier
            .fillMaxSize(),
        factory = { webView },
        update = {
            if (type == "web") {
                it.loadUrl(AppConsts.KB_URL)
            } else if (type == "html") {
                it.loadDataWithBaseURL(null, data.value, "text/html", "UTF-8", null)
            }
        })
    BackHandler(enabled = webHistory?.currentIndex != 0) {
        webView.goBack()
    }
}

@Composable
fun FloatingButton() {
    val context = LocalContext.current
    FloatingActionButton(
        onClick = {
            Toast.makeText(context, "Clicked float button", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier
            .padding(16.dp)
            .size(56.dp)
            .absoluteOffset(16.dp, 16.dp) // Adjust position as needed
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
        )
    }
}



