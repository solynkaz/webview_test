package com.example.webview

import android.content.Context
import android.util.Log
import java.io.File

fun readFileFromInternalStorage(path: String, context: Context): String {
    val filePath = "${context.filesDir}/${AppConsts.KB_FOLDER}${
        path.split("/").dropLast(1).joinToString(separator = "/")
    }";
    val fileName = "${path.split("/").last()}.md"
    Log.i("MarkDown", "Reading from $filePath")
    return File("$filePath/$fileName").bufferedReader().use { it.readText() }.trim()
}

fun isRepoEmpty(path: String, context: Context): Boolean {
    // TODO
    return false
}