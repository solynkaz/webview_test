package com.example.webview

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log

fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    return if (capabilities == null) {
        Log.i("Internet", "No internet connection available.")
        false
    } else {
        true
    }
}