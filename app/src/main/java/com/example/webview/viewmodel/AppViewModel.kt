package com.example.webview.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.webview.AppConsts
import com.example.webview.PAGES
import com.example.webview.PREFS_VALUES
import com.example.webview.PREFS_VALUES.PREFS
import com.example.webview.controller.readFileFromInternalStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

sealed class AppEvent {
    data class LoadSettings(val context: Context) : AppEvent()
    data class SaveSettings(val settingsMap: Map<String, String>, val context: Context) : AppEvent()
    data class LoadLocalFile(
        val currentLocalFile: File,
        val context: Context
    ) : AppEvent()

    data class HistoryBack(val context: Context) : AppEvent()

    data class ChangeDirectory(
        val newDirectory: File
    ) : AppEvent()

    data class LoadHome(val context: Context) : AppEvent()

    data class Login(val login: String, val password: String) : AppEvent()
    data class ChangePageTitle(val title: String) : AppEvent()
}

data class AppState(
    val currentFile: File? = null,
    val currentFileData: String = "",
    val currentDirectory: File? = null,
    val isGitCloned: Boolean = false,
    val history: MutableList<File?> = mutableListOf(),
    val isHistoryPopping: Boolean = false,
    val commonPath: String = "",
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
                val prefs: SharedPreferences = event.context.getSharedPreferences(
                    PREFS,
                    ComponentActivity.MODE_PRIVATE
                )
                val login = prefs.getString(PREFS_VALUES.GIT_LOGIN, "")
                val pass = prefs.getString(PREFS_VALUES.GIT_PASS, "")
                val bearer = prefs.getString(PREFS_VALUES.WIKI_JS_BEARER, "")
                val isGitCloned = prefs.getBoolean(PREFS_VALUES.IS_REPO_CLONED, false)
                appState = appState.copy(
                    login = login!!,
                    password = pass!!,
                    bearer = bearer!!,
                    isGitCloned = isGitCloned
                )
            }

            is AppEvent.ChangeDirectory -> {
                appState = appState.copy(currentDirectory = event.newDirectory)
            }

            is AppEvent.LoadHome -> {
                val commonPath = "${event.context.filesDir}/${AppConsts.GIT_FOLDER}"
                val homeFile = File("$commonPath/home.md")
                Log.i("No connection", "Loading home.md from ${homeFile.path}")
                onEvent(
                    AppEvent.LoadLocalFile(
                        context = event.context,
                        currentLocalFile = homeFile
                    )
                )
                appState = appState.copy(commonPath = commonPath)
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

            is AppEvent.LoadLocalFile -> {
                if (appState.currentFile != event.currentLocalFile) {
                    try {
                        val data = readFileFromInternalStorage(
                            currentLocalFile = event.currentLocalFile
                        )
                        if (!appState.isHistoryPopping && appState.history.size <= 6) {
                            appState.history.add(0, event.currentLocalFile)
                            Log.i(
                                "History",
                                "Adding to history ${event.currentLocalFile.name} \n History: ${appState.history}"
                            )
                        }
                        appState = appState.copy(
                            currentFileData = data,
                            currentFile = event.currentLocalFile,
                            currentDirectory = event.currentLocalFile.parentFile,
                            isHistoryPopping = false
                        )
                    } catch (ex: Exception) {
                        appState = appState.copy(currentFileData = AppConsts.EMPTY_MD_STRING)
                        Log.e("File system", ex.toString())
                    }
                }
            }

            is AppEvent.HistoryBack -> {
                Log.i("History", "History popping... History size: ${appState.history.size}.")
                if (appState.history.size > 1) {
                    appState = appState.copy(isHistoryPopping = true)
                    appState.history.removeFirst()
                    val file = appState.history.first()
                    onEvent(
                        AppEvent.LoadLocalFile(
                            currentLocalFile = file!!,
                            context = event.context
                        )
                    )
                    Log.i("History", "History popped. History size: ${appState.history.size}")
                }
            }

        }
    }
}
