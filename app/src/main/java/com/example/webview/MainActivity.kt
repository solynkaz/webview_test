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
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.webview.controller.isOnline
import com.example.webview.ui.components.MarkDownContent
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
            val context = LocalContext.current
            val isThereNetworkConnection = remember { mutableStateOf(isOnline(context = context)) }
            val firstLaunch = remember { mutableStateOf(true) }
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
    gitViewModel: GitViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel()
) {
    val buttonsVisibility = remember { mutableStateOf(true) }
    val currentFile = remember { mutableStateOf("home") }
    val currentFileExtension = remember { mutableStateOf("md") }

    if (firstLaunch.value) {
        gitViewModel.onEvent(GitRepoEvent.LoadGitSettings(prefs = prefs))
        appViewModel.onEvent(AppEvent.LoadAppSettings(prefs = prefs))
        firstLaunch.value = false
    }

    if (appViewModel.appState.login == "") {
        LoginCompose(isThereNetworkConnection)
    } else {
        if (isThereNetworkConnection.value) {
            WebView(context = context, type = "web")
        } else {
            if (currentFileExtension.value == "md") {
                MarkDownContent(
                    buttonsVisibility,
                    currentFileExtension = currentFileExtension,
                    currentFilePath = currentFile
                )
            } else if (currentFileExtension.value == "html") {
                WebView(context = context, type = "file")
            }
        }
    }
}

@Composable
fun WebView(context: Context, type: String) {
    val mdState: MDState = hiltViewModel<MarkdownViewModel>().mdState
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
            if (type == "file") {
                it.loadDataWithBaseURL(null, mdState.data, "text/html", "UTF-8", null)
            } else {
                it.loadUrl(AppConsts.KB_URL)
            }
        })
    BackHandler(enabled = webHistory?.currentIndex != 0) {
        webView.goBack()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginCompose(
    isThereNetworkConnection: MutableState<Boolean>,
    appViewModel: AppViewModel = hiltViewModel(),
    gitViewModel: GitViewModel = hiltViewModel()
) {
    val login = remember { mutableStateOf("zma@sibdigital.net") }
    val password = remember { mutableStateOf("J0tul8878q1e3") }
    val context = LocalContext.current
    val textFieldModifier = Modifier.fillMaxWidth()
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.weight(1f))
        Text("Git Авторизация", modifier = Modifier.align(CenterHorizontally), fontSize = 24.sp)
        Row() {
            Spacer(Modifier.weight(0.5f))
            OutlinedTextField(
                modifier = textFieldModifier.weight(1f),
                singleLine = true,
                label = {
                    Text(text = "Логин")
                },
                value = login.value,
                onValueChange = { letter -> login.value = letter }
            )
            Spacer(Modifier.weight(0.5f))
        }
        Row() {
            Spacer(Modifier.weight(0.5f))

            OutlinedTextField(
                modifier = textFieldModifier
                    .weight(1f)
                    .align(CenterVertically),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                label = {
                    Text(text = "Пароль")
                },
                value = password.value,
                onValueChange = { letter -> password.value = letter }
            )
            Spacer(Modifier.weight(0.5f))
        }
        Row() {
            Spacer(Modifier.weight(0.5f))
            Button(
                onClick = {
                    appViewModel.onEvent(AppEvent.Login(login.value, password.value))
                    if (!gitViewModel.gitRepoState.isGitUpdatePending && isThereNetworkConnection.value) {
                        gitViewModel.onEvent(
                            GitRepoEvent.GitUpdate(
                                bearer = appViewModel.appState.bearer,
                                context = context,
                                login = appViewModel.appState.login,
                                password = appViewModel.appState.password,
                            )
                        )
                    }
                    //TODO Проверка на валидность
                }, modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f)
                    .padding(top = 15.dp)
            ) {
                Text("Войти")
            }
            Spacer(Modifier.weight(0.5f))
        }
        Spacer(Modifier.weight(1f))
    }
}



