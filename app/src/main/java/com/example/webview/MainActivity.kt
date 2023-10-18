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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsEndWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
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
    gitViewModel: GitViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel()
) {
    val gitRepoState = gitViewModel.gitRepoState
    val buttonsVisibility = remember { mutableStateOf(true) }
    val currentFile = remember { mutableStateOf("home") }
    val currentFileExtension = remember { mutableStateOf("md") }

    if (firstLaunch.value) {
        isThereNetworkConnection.value = isOnline(context)
        gitViewModel.onEvent(GitRepoEvent.LoadGitSettings(prefs = prefs))
        appViewModel.onEvent(AppEvent.LoadAppSettings(prefs = prefs))
        firstLaunch.value = false
    }

    if (appViewModel.appState.login == "") {
        LoginCompose(appViewModel)
    } else {
        if (isThereNetworkConnection.value) {
            if (gitRepoState.isGitClonePending || gitRepoState.isGitClonePending) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        Modifier.align(
                            Center
                        )
                    )
                }
            }
            if (gitRepoState.gitRepoUrl == "" && !gitRepoState.isGetRepoUrlPending) {
                gitViewModel.onEvent(GitRepoEvent.GetRepoUrl(appViewModel.appState.bearer, context))
            }
            if (!gitRepoState.isGitCloneLoaded && !gitRepoState.isGitClonePending && gitRepoState.gitRepoUrl != "") {
                gitViewModel.onEvent(
                    GitRepoEvent.GitRepoClone(
                        context,
                        appViewModel.appState.login,
                        appViewModel.appState.password
                    )
                )
            }
            WebView(context = context, type = "web")
        } else {
//        ControlButtons(currentFilePath, fileContent, buttonsVisibility, coroutineScope)
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
        }
    }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginCompose(appViewModel: AppViewModel) {
    val login = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val textFieldModifier = Modifier.fillMaxWidth()
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.weight(1f))
        Text("Авторизация", modifier = Modifier.align(CenterHorizontally), fontSize = 24.sp)
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


