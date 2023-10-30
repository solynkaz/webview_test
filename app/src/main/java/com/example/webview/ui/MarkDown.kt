package com.example.webview.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.webview.AppConsts
import com.example.webview.viewmodel.AppEvent
import com.example.webview.viewmodel.AppViewModel
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText
import kotlinx.coroutines.launch
import java.io.File

//MarkDown Content
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkDownContent(
    appModel: AppViewModel,
    scrollState: ScrollState,
    drawerState: DrawerState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val data = appModel.appState.currentFileData

    RichText(
        modifier = Modifier
            .padding()
            .padding(all = 15.dp)
            .verticalScroll(scrollState)
    ) {
        Markdown(
            content = data,
            onLinkClicked = { link ->
                if (link.startsWith("http")) {
                    Toast.makeText(context, "Ссылка недоступна.", Toast.LENGTH_SHORT).show()
                } else {
                    coroutineScope.launch {
                        val currentFile = findFile(appModel, link)
                        scrollState.animateScrollTo(0)
                        Log.i("MarkDown", "Link changed to $link")
                        if (currentFile != null) {
                            if (currentFile.isDirectory) {
                                appModel.onEvent(AppEvent.ChangeDirectory(newDirectory = currentFile))
                                drawerState.open()
                            } else {
                                appModel.onEvent(
                                    AppEvent.LoadLocalFile(
                                        context = context,
                                        currentLocalFile = currentFile
                                    )
                                )
                            }
                        } else {
                            appModel.onEvent(AppEvent.SetData(AppConsts.EMPTY_MD_STRING))
                        }
                    }
                }
            })
    }
}

private fun findFile(appModel: AppViewModel, link: String): File? {
    val linkWithoutHooks = link.split("#").first()
    val linkWithoutFileName = linkWithoutHooks.split("/").dropLast(1).joinToString(separator = "/")
    val fileName = linkWithoutHooks.split("/").last()
    val repo = File("${appModel.appState.commonPath}/$linkWithoutFileName")
    var file: File? = null

    for (fileInRepo in repo.listFiles().orEmpty()) {
        if (fileName == fileInRepo.nameWithoutExtension) {
            file = fileInRepo
        }
    }
    return file
}

