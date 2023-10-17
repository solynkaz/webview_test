package com.example.webview

import android.content.Context
import android.content.SharedPreferences
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

suspend fun gitClone(context: Context, login: String, password: String): Boolean {
    try {
        val directoryPath = "${AppConsts.GIT_FOLDER}/"
        val localPath = File(context.filesDir, directoryPath)
        localPath.deleteRecursively()
        if (!localPath.exists()) {
            localPath.mkdirs()
        }
        val remoteRepoURL = "https://gitlab.sibdigital.net/sibdigital/kb.git"
        //TODO Вытащить с ответа

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

suspend fun getRepoURL(bearer: String, context: Context): Boolean {
    val retrofit = GraphQLInstance.graphQLService
    val paramObject = JSONObject()
    val responseRepoUrl: String
    paramObject.put("query", AppConsts.GET_REPO_URL_QUERY)
    val pref: SharedPreferences = context.getSharedPreferences(
        "prefs",
        ComponentActivity.MODE_PRIVATE
    )
    if (pref.getString(PREFS_VALUES.GIT_REPO_URL, "") == "") {
        // TODO Где брать Bearer?
        try {
            val response = Gson().fromJson(
                retrofit.postQuery(bearer, paramObject.toString()).body(),
                root::class.java
            )
            val target = response?.data?.storage?.targets?.find { it.title == "Git" }
            responseRepoUrl = target?.config?.find { it.key == "repoUrl" }?.value?.let {
                JSONObject(it).getString("value")
            }!!
            pref.edit().putString(PREFS_VALUES.GIT_REPO_URL, responseRepoUrl).apply()

        } catch (e: java.lang.Exception) {
            Log.e("Wiki", e.toString())
            return false
        }
    }
    return true
}
fun isOnline(context: Context): Boolean {
//    val connectivityManager =
//        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//    val capabilities =
//        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
//    return if (capabilities == null) {
//        Log.i("Internet", "No internet connection available.")
//        false
//    } else {
//        true
//    }
    return false
}