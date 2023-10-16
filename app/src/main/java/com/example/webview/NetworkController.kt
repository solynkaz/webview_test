package com.example.webview

import android.content.Context
import android.net.ConnectivityManager
import android.util.JsonReader
import android.util.Log
import android.widget.Toast
import com.example.webview.api.GraphQLInstance
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

suspend fun getRepoURL(){
    val retrofit = GraphQLInstance.graphQLService
    val paramObject = JSONObject()
    paramObject.put("query", """
        query {
          storage{
            targets {
              title
              config {
                key
                value
              }
            }
          }
        }
    """.trimIndent())
    val query = paramObject.toString()
    val bearer = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcGkiOjEsImdycCI6MSwiaWF0IjoxNjk3MTY0OTA2LCJleHAiOjE3Mjg3MjI1MDYsImF1ZCI6InVybjp3aWtpLmpzIiwiaXNzIjoidXJuOndpa2kuanMifQ.HFAxu393tjFYz8Q6kIFe8aH0SBL7TLLaeSAhBj4XmUD5UjuhshlVQ9VqQIcQ4b8bqgD5W0_iVCgm-CziE6_0sJGUiO4rKeAuA3GGb7ltIJoa63XMYlrs5yU201iGmAJ5zWS87CIt5D4QaUtdF1CDjZXPwdStjftxe1z3YIWUeAT58yp4aSgRmR9l_bATl7sKnEX9B05vlC2WNFHCs1uiNdSULhJdZ0il9Ib95YqI99fS4a4OG_heaqEpYDPgYuLW0HDKUnqPsdCERwqKRtEhQKfuDsd3A-uFM8TmuAlrWfTrrX6yxu4ym5LMDF1lW2461ljUkQalfvRhRzYd0CKmpg"
    try {
        val response = retrofit.postQuery(bearer, query)
        Log.i("response", "")
    } catch (e: java.lang.Exception){
        Log.e("Wiki", e.toString())
    }
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