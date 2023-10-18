package com.example.webview

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import java.io.File

fun readFileFromInternalStorage(path: String, currentFileExtension: MutableState<String>, context: Context): String {
    val filePath = "${context.filesDir}/${AppConsts.GIT_FOLDER}${
        path.split("/").dropLast(1).joinToString(separator = "/")
    }";
    val fileName = "${path.split("/").last()}"
    val file = findFile(path = filePath, fileName = fileName)
    currentFileExtension.value = file.extension
    Log.i("MarkDown", "Reading from $filePath")
    val parsedFileString = file.bufferedReader().use { it.readText() }.lines().drop(10).joinToString("\n")

    return parsedFileString
}

fun isRepoEmpty(context: Context): Boolean {
    val filePath = "${context.filesDir}/${AppConsts.GIT_FOLDER}";
    val repoFiles = File(filePath).listFiles()

    return repoFiles?.isEmpty() ?: false
}

fun findFile(path: String, fileName: String): File {
    val repo = File(path)
    var file : File? = null
    for (repoFile in repo.listFiles()!!) {
        val repoFileName = repoFile.name.split(".").first()
        if (fileName.split("#").first() == repoFileName) {
            Log.d("debug", "Файл $fileName найден - ${repoFile.name}")
            file = repoFile
            break
        }
    }

    return file ?: throw Exception("File $fileName by path $path in repo not found.")
}