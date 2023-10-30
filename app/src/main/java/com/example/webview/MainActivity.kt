package com.example.webview

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.webview.controller.isOnline
import com.example.webview.ui.MarkDownContent
import com.example.webview.ui.Settings_Screen
import com.example.webview.ui.components.DirectoryNavigation
import com.example.webview.ui.components.WebViewCompose
import com.example.webview.ui.components.WebViewHelper
import com.example.webview.viewmodel.AppEvent
import com.example.webview.viewmodel.AppViewModel
import com.example.webview.viewmodel.GitRepoEvent
import com.example.webview.viewmodel.GitViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


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

            val context = LocalContext.current
            val isThereNetworkConnection = remember { mutableStateOf(isOnline(context = context)) }

            //Листенер интернета
            LaunchedEffect(Unit) {
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                connectivityManager.registerDefaultNetworkCallback(object :
                    ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        isThereNetworkConnection.value = true
                    }

                    override fun onLost(network: Network) {
                        isThereNetworkConnection.value = false
                    }
                })
                appViewModel.onEvent(
                    AppEvent.LoadHome(
                        context = context,
                    )
                )
                gitViewModel.onEvent(GitRepoEvent.LoadGitSettings(context))
            }

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            val scope = rememberCoroutineScope()

            Surface(Modifier.fillMaxSize()) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(appViewModel.appState.pageTitle) },
                            colors = TopAppBarDefaults.smallTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary
                            ),
                            actions = {
                                if (gitViewModel.gitRepoState.isGitClonePending) {
                                    CircularProgressIndicator()
                                }
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
                            NavHost(
                                navController = navController,
                                startDestination = "Main_Screen"
                            ) {
                                composable("Main_Screen") {
                                    Main_Screen(
                                        isThereNetworkConnection = isThereNetworkConnection,
                                        context = context,
                                        scaffoldPadding = padding,
                                        gitViewModel = gitViewModel,
                                        appViewModel = appViewModel,
                                        drawerState = drawerState
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
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Main_Screen(
        isThereNetworkConnection: MutableState<Boolean>,
        context: Context,
        scaffoldPadding: PaddingValues,
        gitViewModel: GitViewModel,
        appViewModel: AppViewModel,
        drawerState: DrawerState
    ) {
        val localFilesSrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            appViewModel.onEvent(AppEvent.LoadSettings(context))
            if (appViewModel.appState.bearer != "" && gitViewModel.gitRepoState.gitRepoUrl == "") {
                gitViewModel.onEvent(
                    GitRepoEvent.GetRepoUrl(
                        bearer = appViewModel.appState.bearer,
                        context = context,
                        appViewModel = appViewModel
                    )
                )
            }
        }
        if (!gitViewModel.gitRepoState.isGetRepoUrlPending) {
            LaunchedEffect(Unit) {
                //Попытка клонировать репо
                if (!appViewModel.appState.isGitCloned && checkSettings(
                        appViewModel,
                        gitViewModel
                    )
                ) {
                    if (!gitViewModel.gitRepoState.isGitClonePending) {
                        gitViewModel.onEvent(
                            GitRepoEvent.GitClone(
                                context = context,
                                login = appViewModel.appState.login,
                                password = appViewModel.appState.password,
                                urlRepo = gitViewModel.gitRepoState.gitRepoUrl,
                                appViewModel = appViewModel
                            )
                        )
                    }
                }
                if (appViewModel.appState.isGitCloned && !gitViewModel.gitRepoState.isGitFetchPending && checkSettings(
                        appViewModel,
                        gitViewModel
                    )
                ) {
                    //Попытка сделать fetch репозитория
                    gitViewModel.onEvent(
                        GitRepoEvent.GitFetch(
                            login = appViewModel.appState.login,
                            password = appViewModel.appState.password,
                            context = context
                        )
                    )
                }
            }
        }
        Box(
            Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            if (isThereNetworkConnection.value) {
                WebViewHelper()
            } else {
                BackHandler {
                    coroutineScope.launch {
                        localFilesSrollState.animateScrollTo(0)
                        appViewModel.onEvent(AppEvent.HistoryBack(context))
                    }
                }
                if (appViewModel.appState.currentFile?.extension == "md" || appViewModel.appState.currentFile == null) {
                    MarkDownContent(
                        appModel = appViewModel,
                        scrollState = localFilesSrollState,
                        drawerState = drawerState
                    )
                } else if (appViewModel.appState.currentFile?.extension == "html") {
                    WebViewCompose(
                        context = context,
                        appViewModel = appViewModel,
                        isThereNetworkConnection = isThereNetworkConnection
                    )
                }
// Проверить автоматическое клонирование в фоне при запуске приложения
            }
        }
    }

    private fun checkSettings(appViewModel: AppViewModel, gitViewModel: GitViewModel): Boolean {
        val appState = appViewModel.appState
        val gitState = gitViewModel.gitRepoState

        return appState.login != "" &&
                appState.password != "" &&
                gitState.gitRepoUrl != ""
    }
}
