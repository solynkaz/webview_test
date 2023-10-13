package com.example.webview.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.webview.AppConsts
import com.example.webview.gitClone
import com.example.webview.isOnline
import com.example.webview.readFileFromInternalStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlButtons(
    currentFilePath: MutableState<String>,
    mdContent: MutableState<String>,
    buttonsVisibility: MutableState<Boolean>,
    coroutineScope: CoroutineScope
) {
    val login = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val passwordVisibility = remember { mutableStateOf(false) } // TODO Добавить функционал
    val buttonModifier : Modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp)
    val context = LocalContext.current
    Column() {
        Row(modifier = Modifier.padding(vertical = 10.dp)) {
            OutlinedTextField(
//                enabled = !isLoadingState,
                enabled = true,
                modifier = buttonModifier.weight(0.5f, true),
                singleLine = true,
                label = {
                    Text(text = "Логин")
                },
                value = login.value,
                onValueChange = { letter -> login.value = letter }
            )
            OutlinedTextField(
                //                enabled = !isLoadingState,
                enabled = true,
                modifier = buttonModifier.weight(0.5f, true),
                singleLine = true,
                visualTransformation = if (passwordVisibility.value) VisualTransformation.None else PasswordVisualTransformation(),
                label = {
                    Text(text = "Пароль")
                },
                value = password.value,
                onValueChange = { letter -> password.value = letter }
            )
        }
        Row() {
            FloatingActionButton(
                onClick = {
                    writeFileToInternalStorage(
                        "/общие-принципы/wiki/Правила-написания-страниц",
                        context
                    )
                },
            ) {
                Icon(Icons.Filled.Add, "Write File")
            }
            FloatingActionButton(
                onClick = {
                    mdContent.value = readFileFromInternalStorage(currentFilePath.value, context)
                    buttonsVisibility.value = false
                },
            ) {
                Icon(Icons.Filled.Home, "Parse Home.md")
            }
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        gitClone(context = context, login.value, password.value)
                        Toast.makeText(context, "Git repository was cloned successfully", Toast.LENGTH_SHORT).show()
                    }
                })
            {
                Icon(Icons.Filled.Send, "Clone git repo")
            }
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        val directoryPath = "${AppConsts.GIT_FOLDER}/"
                        val localPath = File(context.filesDir, directoryPath)
                        localPath.deleteRecursively()
                    }
                })
            {
                Icon(Icons.Filled.Delete, "Clean git repo directory")
            }
            FloatingActionButton(
                onClick = {
                    val dirs = context.filesDir
                    Log.i("GIT", "Internal storage $dirs")
                })
            {
                Icon(Icons.Filled.Search, "Lookup internal storage")
            }
            FloatingActionButton(
                onClick = {
                    isOnline(context)
                })
            {
                Icon(Icons.Filled.Star, "Check internet connection")
            }
        } // Button Row
    }
}



fun writeFileToInternalStorage(path: String, context: Context) {
    val fileName = "${path.split("/").last()}.md"
    val directoryPath = "${AppConsts.KB_FOLDER}${path.split("/").dropLast(1).joinToString(separator = "/")}"
    val dir = File(context.filesDir, directoryPath)
    if (!dir.exists()) {
        dir.mkdirs()
    }
    try {
        val fileToWrite = File(dir, fileName)
        if (!fileToWrite.exists()) {
            val writer = FileWriter(fileToWrite)
            writer.append(
                context.assets.open(fileToWrite.name).bufferedReader().use { it.readText() }.trim()
            )
            writer.flush()
            writer.close()
            Log.i("MarkDown", "File $fileName written on path $directoryPath")
        } else {
            Log.i("MarkDown", "File $fileName already exists on path $directoryPath")
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}