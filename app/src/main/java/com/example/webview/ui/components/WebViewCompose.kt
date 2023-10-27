package com.example.webview.ui.components

import android.content.Context
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebBackForwardList
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.webview.AppConsts
import com.example.webview.viewmodel.AppViewModel

val offlineModifier = Modifier.fillMaxSize()

@Composable
fun WebViewCompose(
    context: Context,
    type: String,
    isThereNetworkConnection: MutableState<Boolean>,
    appViewModel: AppViewModel
) {
    val data = remember { mutableStateOf(appViewModel.appState.currentFileData) }
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    val webView = remember {
        WebView(context).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.setSupportZoom(true)
            settings.useWideViewPort = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.safeBrowsingEnabled = false
            settings.databasePath = "/data/user/0/com.example.webview/database/"
            settings.loadsImagesAutomatically = true
            settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
            settings.allowContentAccess = true
            settings.allowFileAccess = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            settings.disabledActionModeMenuItems = WebSettings.MENU_ITEM_NONE
            settings.mediaPlaybackRequiresUserGesture = true
            settings.setNeedInitialFocus(true)
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 13; M2101K7BNY Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/118.0.0.0 Mobile Safari/537.36"
            webChromeClient = object :
                WebChromeClient() {
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
        modifier = if (!isThereNetworkConnection.value) {
            Modifier
                .fillMaxSize()
                .verticalScroll(verticalScroll)
                .horizontalScroll(horizontalScroll)
        } else {
            Modifier.fillMaxSize()
        },
        factory = { webView },
        update = {
            Log.i("WebViewDebug", "Current URL:${it.url}")
            if (type == "web") {
                it.loadUrl(AppConsts.KB_URL)
            } else if (type == "html") {
                it.loadDataWithBaseURL(null, data.value, "text/html", "UTF-8", null)
            }
        })
}