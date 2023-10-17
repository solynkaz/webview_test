package com.example.webview.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webview.PREFS_VALUES
import com.example.webview.getRepoURL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GitRepoEvent {
    data class GetRepoUrl(val bearer: String, val context: Context) : GitRepoEvent()
    data class GitRepoClone(val repoUrl: String) : GitRepoEvent()

}
data class GitRepoState(
    val gitRepoUrl: String? = null,
    val isGetRepoUrlPending: Boolean = false,
    val isGitClonePending: Boolean = false,
    val isGitRepoUrlLoaded: Boolean = false,
    val isGitCloneLoaded: Boolean = false,
)

@HiltViewModel
class GitViewModel @Inject constructor(): ViewModel() {

    var gitRepoState by mutableStateOf(GitRepoState())
        private set

    fun onEvent(event: GitRepoEvent) {
        when (event) {
            is GitRepoEvent.GetRepoUrl -> {
                gitRepoState = gitRepoState.copy(isGetRepoUrlPending = true)
                viewModelScope.launch {
                    val gitUrlResponse = getRepoURL(event.bearer, event.context)
                    val pref: SharedPreferences = event.context.getSharedPreferences(
                        "prefs",
                        ComponentActivity.MODE_PRIVATE
                    )
                    gitRepoState = gitRepoState.copy(isGetRepoUrlPending = false,
                        isGitRepoUrlLoaded = gitUrlResponse)
                    if (gitRepoState.isGitRepoUrlLoaded) {
                        pref.getString(PREFS_VALUES.GIT_REPO_URL, "")
                    }
                }
            }
            is GitRepoEvent.GitRepoClone -> {
            }
            else -> {}
        }
    }
}
