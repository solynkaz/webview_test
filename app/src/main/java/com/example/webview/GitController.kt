package com.example.webview

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

suspend fun gitClone(context: Context, login: String, password: String): Boolean {
    try {
        val directoryPath = "${AppConsts.GIT_FOLDER}/"
        val localPath = File(context.filesDir, directoryPath)
        localPath.deleteRecursively()
        if (!localPath.exists()) {
            localPath.mkdirs()
        }
        val remoteRepoURL = "https://gitlab.sibdigital.net/sibdigital/kb.git"

        val credentialsProvider = UsernamePasswordCredentialsProvider(login, password)
        withContext(Dispatchers.IO) {
            val cloneCommand = CloneCommand()
                .setURI(remoteRepoURL)
                .setDirectory(localPath)
                .setCredentialsProvider(credentialsProvider)
            cloneCommand.call()
        }
        Log.i("GIT", "Git was cloned with success.")
        return true
    } catch (ex: Exception) {
        Toast.makeText(context, "Error while git clone", Toast.LENGTH_SHORT).show()
        Log.e("GIT", ex.toString())
        return false
    }
}