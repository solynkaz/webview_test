package com.example.webview.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.webview.PAGES
import com.example.webview.PREFS_VALUES
import com.example.webview.PREFS_VALUES.PREFS
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

sealed class AppEvent {
    data class LoadSettings(val prefs: SharedPreferences) : AppEvent()
    data class SaveSettings(val settingsMap: Map<String, String>, val context: Context): AppEvent()
    data class Login(val login: String, val password: String) : AppEvent()
    data class ChangePageTitle(val title: String) : AppEvent()
}

data class AppState(
    val currentFile: File? = null,
    val pageTitle: String = PAGES.MAIN_MENU,
    val login: String = "21",
    val password: String = "",
    val bearer: String = "",
)

@HiltViewModel
class AppViewModel @Inject constructor() : ViewModel() {

    var appState by mutableStateOf(AppState())
        private set

    fun onEvent(event: AppEvent) {
        when (event) {
            is AppEvent.LoadSettings -> {
                val login = event.prefs.getString(PREFS_VALUES.GIT_LOGIN, "")
                val pass = event.prefs.getString(PREFS_VALUES.GIT_PASS, "")
                val bearer = event.prefs.getString(PREFS_VALUES.WIKI_JS_BEARER, "")
                appState = appState.copy(
                    login = login!!,
                    password = pass!!,
                    bearer = bearer!!
                )
            }
            is AppEvent.Login -> {
                // TODO Авторизация на апи гитлаба
                appState = appState.copy(
                    login = event.login,
                    password = event.password
                )
            }
            is AppEvent.ChangePageTitle -> {
                appState = appState.copy(
                    pageTitle = event.title
                )
            }
            is AppEvent.SaveSettings -> {
                val pref: SharedPreferences = event.context.getSharedPreferences(
                    PREFS,
                    ComponentActivity.MODE_PRIVATE
                )
                for (entry in event.settingsMap) {
                    pref.edit().putString(entry.key, entry.value).apply()
                }
                Toast.makeText(event.context, "Успешно сохранено", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
