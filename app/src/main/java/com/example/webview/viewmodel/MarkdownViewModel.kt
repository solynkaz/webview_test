package com.example.webview.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webview.controller.readFileFromInternalStorage
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val history: Boolean = false
)

@HiltViewModel
class MarkdownViewModel @Inject constructor() : ViewModel() {

    var mdState by mutableStateOf(MDState())
        private set

    fun onEvent(event: MDEvent) {
        when (event) {
            is MDEvent.loadFile -> {
                viewModelScope.launch {
                    try {
                        val data = readFileFromInternalStorage(
                            context = event.context,
                            currentFileExtension = event.currentFileExtension,
                            path = event.currentFilePath.value
                        )
                        mdState = mdState.copy(data = data)
                    } catch (ex: Exception) {
                        Toast.makeText(event.context, "Ссылка недоступна", Toast.LENGTH_SHORT).show()
                        Log.e("File system", ex.toString())
                    }
                }
            }
        }
    }
}
