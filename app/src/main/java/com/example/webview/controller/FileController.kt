package com.example.webview.controller

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.webview.AppConsts
import com.example.webview.PREFS_VALUES
import java.io.File

fun readFileFromInternalStorage(currentLocalFile: File): String {
    Log.i("No connection", "Reading from ${currentLocalFile.path}")
    val parsedFileString = currentLocalFile.bufferedReader().use { it.readText() }.lines().drop(10).joinToString("\n")

    return parsedFileString
}

fun clearGitRepo(context: Context) {
    val filePath = "${context.filesDir}/${AppConsts.GIT_FOLDER}";
    val repo = File(filePath)
    repo.deleteRecursively()
    val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_VALUES.PREFS,
        ComponentActivity.MODE_PRIVATE
    )
    prefs.edit().putBoolean(PREFS_VALUES.IS_REPO_CLONED, true).apply()
    Log.i("Git", "Cache cleared.")
}

fun isRepoEmpty(context: Context): Boolean {
    val filePath = "${context.filesDir}/${AppConsts.GIT_FOLDER}";
    val repoFiles = File(filePath).listFiles()

    return repoFiles?.isEmpty() ?: true
}
