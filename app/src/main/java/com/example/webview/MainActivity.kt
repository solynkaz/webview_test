package com.example.webview

import android.content.Context
import android.os.Bundle
import android.provider.Settings.Global
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText
import kotlinx.coroutines.AbstractCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import retrofit2.Retrofit
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

val KB_FOLDER = "KnowledgeBase"
val GIT_FOLDER = "Git"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val coroutineScope = rememberCoroutineScope()
            val buttonsVisibility = remember { mutableStateOf(true) }
            val mdContent = remember { mutableStateOf("") }
            val currentFilePath = remember { mutableStateOf("/home") }
            if (buttonsVisibility.value) {
                ControlButtons(currentFilePath, mdContent, buttonsVisibility, coroutineScope)
            } else {
                MarkDownContent(mdContent.value, buttonsVisibility, currentFilePath)
            }
        }
    }
}

suspend fun gitClone(context: Context): Boolean {
    try {
        val directoryPath = "$GIT_FOLDER/"
        val localPath = File(context.filesDir, directoryPath)
        localPath.deleteRecursively()
        if (!localPath.exists()) {
            localPath.mkdirs()
        }
        val remoteRepoURL =
            "https://github.com/solynkaz/urban-engine.git"  // Change this to the Git repository URL
//                            val username = "your_username"  // Your Git username
//                            val password = "your_password"  // Your Git password

//                            val credentialsProvider = UsernamePasswordCredentialsProvider(username, password)
        var git: Git
        withContext(Dispatchers.IO) {
            val cloneCommand = CloneCommand()
                .setURI(remoteRepoURL)
                .setDirectory(localPath)
            git = cloneCommand.call()
        }
//                                .setCredentialsProvider(credentialsProvider)
        Log.i("GIT", "Git was cloned with success.")
        return true
    } catch (ex: Exception) {
        Log.e("GIT", ex.toString())
        return false
    }

    // Now you have the Git repository cloned to the localPath
}


@Composable
fun ControlButtons(
    currentFilePath: MutableState<String>,
    mdContent: MutableState<String>,
    buttonsVisibility: MutableState<Boolean>,
    coroutineScope: CoroutineScope
) {
    val context = LocalContext.current
    Row() {
        FloatingActionButton(
//                        onClick = { writeFileToInternalStorage(currentFilePath.value, context) },
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
            Icon(Icons.Filled.Add, "Parse File")
        }
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    gitClone(context = context)
                }
            })
        {
            Icon(Icons.Filled.Call, "Parse File")
        }
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    val directoryPath = "$GIT_FOLDER/"
                    val localPath = File(context.filesDir, directoryPath)
                    localPath.deleteRecursively()
                }
            })
        {
            Icon(Icons.Filled.Delete, "Parse File")
        }
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    val dirs = context.filesDir
                    Log.i("GIT", "Internal storage $dirs")
                }
            })
        {
            Icon(Icons.Filled.Search, "Parse File")
        }
    }
}

fun readFileFromInternalStorage(path: String, context: Context): String {
    val filePath = "${context.filesDir}/$KB_FOLDER${
        path.split("/").dropLast(1).joinToString(separator = "/")
    }";
    val fileName = "${path.split("/").last()}.md"
    Log.i("MarkDown", "Reading from $filePath")
    return File("$filePath/$fileName").bufferedReader().use { it.readText() }.trim()
}

fun writeFileToInternalStorage(path: String, context: Context) {
    val fileName = "${path.split("/").last()}.md"
    val directoryPath = "$KB_FOLDER${path.split("/").dropLast(1).joinToString(separator = "/")}"
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

//MarkDown Content
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkDownContent(
    mdFile: String,
    buttonsVisibility: MutableState<Boolean>,
    currentFilePath: MutableState<String>
) {
    var markDownString = remember { mutableStateOf(mdFile) }
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