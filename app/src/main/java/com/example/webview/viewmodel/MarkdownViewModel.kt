package com.example.webview.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webview.PREFS_VALUES
import com.example.webview.getRepoURL
import com.example.webview.gitClone
import com.example.webview.isRepoEmpty
import com.example.webview.readFileFromInternalStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MDEvent {
    data class loadFile(
        val currentFilePath: MutableState<String>,
        val currentFileExtension: MutableState<String>,
        val context: Context
    ) : MDEvent()

}

data class MDState(
    val data: String = "",
    val isLoading: Boolean = false,
    val history: Boolean = false
)

@HiltViewModel
class MarkdownViewModel @Inject constructor() : ViewModel() {

    var mdState by mutableStateOf(MDState())
        private set

    fun onEvent(event: MDEvent) {
        when (event) {
            is MDEvent.loadFile -> {
                mdState = mdState.copy(isLoading = true)
                viewModelScope.launch {
                    try {
                        val data = readFileFromInternalStorage(
                            context = event.context,
                            currentFileExtension = event.currentFileExtension,
                            path = event.currentFilePath.value
                        )
                        mdState = mdState.copy(data = data)
                    } catch (ex: Exception) {
                        Log.e("File system", ex.toString())
                    } finally {
                        mdState = mdState.copy(isLoading = false)
                    }
                }
            }

            else -> {
                Log.e("Event handler", "No such event: $event")
            }
        }
    }
}
