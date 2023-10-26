package com.example.webview.controller

import android.content.Context
import android.util.Log
import com.example.webview.AppConsts
import java.io.File

fun readFileFromInternalStorage(currentLocalFile: File): String {
//
//
//
//    val filePath = "${context.filesDir}/${AppConsts.GIT_FOLDER}${
//        path.split("/").dropLast(1).joinToString(separator = "/")
//    }";
//    val fileName = "${path.split("/").last()}"
//    val file = findFile(path = filePath, fileName = fileName)
//    currentFileExtension.value = file.extension
    Log.i("No connection", "Reading from ${currentLocalFile.path}")
    val parsedFileString = currentLocalFile.bufferedReader().use { it.readText() }.lines().drop(10).joinToString("\n")

    return parsedFileString
}

fun clearGitRepo(context: Context) {
    val filePath = "${context.filesDir}/${AppConsts.GIT_FOLDER}";
    val repo = File(filePath)
    repo.deleteRecursively()
    Log.i("Git", "Cache cleared.")
}

fun isRepoEmpty(context: Context): Boolean {
    val filePath = "${context.filesDir}/${AppConsts.GIT_FOLDER}";
    val repoFiles = File(filePath).listFiles()

    return repoFiles?.isEmpty() ?: true
}
