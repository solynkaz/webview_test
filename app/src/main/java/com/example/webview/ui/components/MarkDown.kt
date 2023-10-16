package com.example.webview.ui.components

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.webview.readFileFromInternalStorage
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText

//MarkDown Content
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkDownContent(
    mdFile: String,
    buttonsVisibility: MutableState<Boolean>,
    currentFilePath: MutableState<String>
) {
    val markDownString = remember { mutableStateOf(mdFile) }
    val context = LocalContext.current
    Scaffold(
        content = { padding ->
            RichText(
                modifier = Modifier
                    .padding(padding)
                    .padding(all = 15.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Markdown(
                    content = markDownString.value,
                    onLinkClicked = { link ->
                        currentFilePath.value = link
                        Log.i("MarkDown", "Link changed to ${currentFilePath.value}")
                        markDownString.value =
                            readFileFromInternalStorage(currentFilePath.value, context)
                    })
            }
        },
        floatingActionButton = {
            Button(onClick = {
                buttonsVisibility.value = true
            }) {

            }
        },
        ///общие-принципы/wiki/Синтаксис-Markdown
        modifier = Modifier
            .padding(1.dp)
    )
}