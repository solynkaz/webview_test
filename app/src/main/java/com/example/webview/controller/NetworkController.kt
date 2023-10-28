package com.example.webview.controller

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.webview.AppConsts
import com.example.webview.PREFS_VALUES
import com.example.webview.PREFS_VALUES.PREFS
import com.example.webview.api.GraphQLInstance
import com.example.webview.api.models.root
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.json.JSONObject
import java.io.File

suspend fun getRepoURL(bearer: String, context: Context): Boolean {
    val retrofit = GraphQLInstance.graphQLService
    val paramObject = JSONObject()
    val responseRepoUrl: String
    paramObject.put("query", AppConsts.GET_REPO_URL_QUERY)
    val pref: SharedPreferences = context.getSharedPreferences(
        PREFS,
        ComponentActivity.MODE_PRIVATE
    )
    try {
        val response = Gson().fromJson(
            retrofit.postQuery("Bearer $bearer", paramObject.toString()).body(),
            root::class.java
        )
        val target = response?.data?.storage?.targets?.find { it.title == "Git" }
        responseRepoUrl = target?.config?.find { it.key == "repoUrl" }?.value?.let {
            JSONObject(it).getString("value").removeSuffix(".git")
        }!!
        pref.edit().putString(PREFS_VALUES.GIT_REPO_URL, responseRepoUrl).apply()
    } catch (e: java.lang.Exception) {
        Log.e("Wiki", e.toString())
        return false
    }
    return true
}

suspend fun gitClone(context: Context, repoUrl: String, login: String, password: String): Boolean {
    try {
        val directoryPath = "${AppConsts.GIT_FOLDER}/"
        val localPath = File(context.filesDir, directoryPath)
        localPath.deleteRecursively()
        if (!localPath.exists()) {
            localPath.mkdirs()
        }

        val credentialsProvider = UsernamePasswordCredentialsProvider(login, password)
        withContext(Dispatchers.IO) {
            val cloneCommand = CloneCommand()
                .setURI(repoUrl)
                .setDirectory(localPath)
                .setCredentialsProvider(credentialsProvider)
            cloneCommand.call().close()
        }
        Toast.makeText(context, "База знаний успешно кэширована.", Toast.LENGTH_SHORT).show()
        return true
    } catch (ex: Exception) {
        Toast.makeText(context, "Git clone error", Toast.LENGTH_SHORT).show()
        Log.e("Git", ex.toString())
        return false
    }
}

suspend fun gitFetch(context: Context, repoUrl: String, login: String, password: String): Boolean {
    Log.i("Git", "Start fetching from $repoUrl")
    try {
        val directoryPath = "${AppConsts.GIT_FOLDER}/"
        val localPath = File(context.filesDir, "$directoryPath/.git")
        val credentialsProvider = UsernamePasswordCredentialsProvider(login, password)
        var git: Git? = null

        try {
            val repo = FileRepositoryBuilder().setGitDir(localPath).build()
            git = Git.wrap(repo)
        } catch (ex: Exception) {
            Log.e("Git", "Error while fetching: Invalid git repository")
        }

        try {
            withContext(Dispatchers.IO) {
                val pull = git?.pull()
                    ?.setCredentialsProvider(credentialsProvider)
                    ?.call()
                val fetchResult = pull?.fetchResult
                Log.i("Git", "Fetch result: $fetchResult")
            }
        } catch (ex: Exception) {
            Log.e("Git", "Error while fetching: $ex")
        } finally {
            git?.close()
        }
        return true
    } catch (ex: Exception) {
        Log.e("Git", ex.toString())
        return false
    }
}

fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    Log.i("Network", "Network availiable = ${capabilities != null}")

    return capabilities != null
}