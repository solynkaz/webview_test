package com.example.webview

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.webview.api.GraphQLInstance
import com.example.webview.api.models.root
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.json.JSONObject
import java.io.File

suspend fun getRepoURL(bearer: String, context: Context): Boolean {
    val retrofit = GraphQLInstance.graphQLService
    val paramObject = JSONObject()
    val responseRepoUrl: String
    paramObject.put("query", AppConsts.GET_REPO_URL_QUERY)
    val pref: SharedPreferences = context.getSharedPreferences(
        "prefs",
        ComponentActivity.MODE_PRIVATE
    )
    val currentSetting = pref.getString(PREFS_VALUES.GIT_REPO_URL, "")
    if (currentSetting == "") {
        // TODO Где брать Bearer?
        try {
            val response = Gson().fromJson(
                retrofit.postQuery(bearer, paramObject.toString()).body(),
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
            cloneCommand.call()
        }
        Toast.makeText(context, "Git was cloned with success", Toast.LENGTH_SHORT).show()
        val pref: SharedPreferences = context.getSharedPreferences(
            "prefs",
            ComponentActivity.MODE_PRIVATE
        )
        pref.edit().putBoolean(PREFS_VALUES.IS_REPO_CLONED, true).apply()
        return true
    } catch (ex: Exception) {
        Toast.makeText(context, "Error while git clone", Toast.LENGTH_SHORT).show()
        Log.e("Git", ex.toString())
        return false
    }
}

suspend fun gitFetch() {
    //TODO
}

fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    return if (capabilities == null) {
        Log.i("App", "No internet connection available.")
        false
    } else {
        Log.i("App", "Internet connection is available")
        true
    }
}