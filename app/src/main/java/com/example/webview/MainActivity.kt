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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.webview.PREFS_VALUES.PREFS
import com.example.webview.controller.isOnline
import com.example.webview.ui.MarkDownContent
import com.example.webview.ui.Settings_Screen
import com.example.webview.ui.components.DirectoryNavigation
import com.example.webview.viewmodel.AppEvent
import com.example.webview.viewmodel.AppViewModel
import com.example.webview.viewmodel.GitRepoEvent
import com.example.webview.viewmodel.GitViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File


val drawerSheetPadding = Modifier.padding(start = 5.dp)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val appViewModel: AppViewModel = hiltViewModel()
            val gitViewModel: GitViewModel = hiltViewModel()

            val navController = rememberNavController()

            val isLoading = remember { mutableStateOf(false) }
            val context = LocalContext.current
            val isThereNetworkConnection = remember { mutableStateOf(isOnline(context = context)) }

            LaunchedEffect(Unit) {
                appViewModel.onEvent(
                    AppEvent.LoadHome(
                        context = context,
                    )
                )
            }

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            val scope = rememberCoroutineScope()

            val prefs: SharedPreferences = context.getSharedPreferences(
                PREFS,
                MODE_PRIVATE
            )

            Scaffold(
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            if (isLoading.value) {
                                CircularProgressIndicator()
                            }
                        },
                        title = { Text(appViewModel.appState.pageTitle) },
                        colors = TopAppBarDefaults.smallTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ),
                        actions = {
                            IconButton(onClick = {
                                scope.launch {
                                    navController.navigate("Settings_Screen") {
                                        launchSingleTop = true
                                    }
                                    appViewModel.onEvent(AppEvent.ChangePageTitle(PAGES.SETTINGS))
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Settings"
                                )
                            }
                        },
                    )
                },
                content = { padding ->
                    ModalNavigationDrawer(
                        drawerContent = {
                            ModalDrawerSheet(
                                modifier = Modifier.padding(padding)
                            ) {
                                DirectoryNavigation(
                                    drawerState = drawerState,
                                    coroutineScope = scope,
                                    appViewModel = appViewModel
                                )
                            }
                        },
                        drawerState = drawerState,
                        gesturesEnabled = !isThereNetworkConnection.value
                    ) {
                        NavHost(navController = navController, startDestination = "Main_Screen") {
                            composable("Main_Screen") {
                                Main_Screen(
                                    isThereNetworkConnection = isThereNetworkConnection,
                                    context = context,
                                    prefs = prefs,
                                    scaffoldPadding = padding,
                                    gitViewModel = gitViewModel,
                                    appViewModel = appViewModel
                                )
                            }
                            composable("Settings_Screen") {
                                Settings_Screen(
                                    isThereNetworkConnection,
                                    innerPaddingValues = padding,
                                )
                                BackHandler(true) {
                                    scope.launch {
                                        appViewModel.onEvent(AppEvent.ChangePageTitle(PAGES.MAIN_MENU))
                                        navController.popBackStack()
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxSize()
            )

        }
    }

    @Composable
    fun Main_Screen(
        isThereNetworkConnection: MutableState<Boolean>,
        context: Context,
        prefs: SharedPreferences,
        scaffoldPadding: PaddingValues,
        gitViewModel: GitViewModel,
        appViewModel: AppViewModel
    ) {

        //Вызов один раз, для вызова при изменении чего-то - вместо Unit
        // указывается элемент за изменением которого необходимо следить
        LaunchedEffect(Unit) {
            val appState = appViewModel.appState
            gitViewModel.onEvent(
                GitRepoEvent.GetRepoUrl(
                    bearer = appState.bearer,
                    context = context
                )
            )
            appViewModel.onEvent(AppEvent.LoadSettings(prefs = prefs))
            gitViewModel.onEvent(
                GitRepoEvent.GitFetch(
                    login = appState.login,
                    password = appState.password,
                    context = context
                )
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            if (isThereNetworkConnection.value) {
                WebViewCompose(context = context, type = "web", appViewModel = appViewModel, isThereNetworkConnection = isThereNetworkConnection)
            } else {
                BackHandler {
                    appViewModel.onEvent(AppEvent.HistoryBack(context))
                }
                if (appViewModel.appState.currentFile?.extension == "md") {
                    MarkDownContent(
                        appModel = appViewModel
                    )
                } else if (appViewModel.appState.currentFile?.extension == "html") {
                    WebViewCompose(context = context, type = "html", appViewModel = appViewModel, isThereNetworkConnection = isThereNetworkConnection)
                }

            }
        }
    }

    @Composable
    fun WebViewCompose(
        context: Context,
        type: String,
        isThereNetworkConnection: MutableState<Boolean>,
        appViewModel: AppViewModel
    ) {
        val data = remember { mutableStateOf(appViewModel.appState.currentFileData) }
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
        if (isThereNetworkConnection.value) {
            BackHandler(enabled = webHistory?.currentIndex != 0) {
                webView.goBack()
            }
        }
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
    }
}
