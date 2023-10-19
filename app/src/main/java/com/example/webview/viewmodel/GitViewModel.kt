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
import androidx.lifecycle.viewModelScope
import com.example.webview.PREFS_VALUES
import com.example.webview.getRepoURL
import com.example.webview.gitClone
import com.example.webview.gitFetch
import com.example.webview.isRepoEmpty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class GitRepoEvent {
    data class GetRepoUrl(val bearer: String, val context: Context) : GitRepoEvent()
    data class LoadGitSettings(val prefs: SharedPreferences) : GitRepoEvent()

    data class GitFetch(val context: Context, val login: String, val password: String) :
        GitRepoEvent()

    data class GitRepoClone(val context: Context, val login: String, val password: String) :
        GitRepoEvent()
}

data class GitRepoState(
    val gitRepoUrl: String? = "",
    val isGetRepoUrlPending: Boolean = false,

    val isGitClonePending: Boolean = false,
    val isGitCloneLoaded: Boolean = false,

    val isGitFetchPending: Boolean = false,
    val isGitFetched: Boolean = false,
)

@HiltViewModel
class GitViewModel @Inject constructor() : ViewModel() {

    var gitRepoState by mutableStateOf(GitRepoState())
        private set

    fun onEvent(event: GitRepoEvent) {
        when (event) {
            is GitRepoEvent.GetRepoUrl -> {
                gitRepoState = gitRepoState.copy(isGetRepoUrlPending = true)
                Log.i("Git", "Started getting repo url...")
                viewModelScope.launch {
                    val gitUrlResponse = getRepoURL(event.bearer, event.context)
                    val pref: SharedPreferences = event.context.getSharedPreferences(
                        "prefs",
                        ComponentActivity.MODE_PRIVATE
                    )
                    gitRepoState = gitRepoState.copy(
                        isGetRepoUrlPending = false,
                        gitRepoUrl = pref.getString(PREFS_VALUES.GIT_REPO_URL, "")
                    )
                    Log.i(
                        "Git",
                        "Repo url updated: ${pref.getString(PREFS_VALUES.GIT_REPO_URL, "")}"
                    )
                }
            }

            is GitRepoEvent.GitRepoClone -> {
                if (isRepoEmpty(event.context)) {
                    Log.i("Git", "Start cloning repository...")
                    gitRepoState = gitRepoState.copy(isGitClonePending = true)
                    viewModelScope.launch {
                        val response = gitClone(
                            context = event.context,
                            repoUrl = gitRepoState.gitRepoUrl!!,
                            login = event.login,
                            password = event.password
                        )
                        if (response) {
                            gitRepoState =
                                gitRepoState.copy(
                                    isGitClonePending = false,
                                    isGitCloneLoaded = true
                                )
                            Log.i("Git", "Cloned with success")
                        } else {
                            Toast.makeText(
                                event.context,
                                "Error while git clone",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            Log.i("Git", "Error while git clone")
                            gitRepoState = gitRepoState.copy(isGitClonePending = false)
                        }
                    }
                } else {
                    gitRepoState = gitRepoState.copy(isGitCloneLoaded = true)
                    Log.i("Git", "Git repository already exists, clone was not called")
                }
            }

            is GitRepoEvent.LoadGitSettings -> {
                Log.i("Git", "Loading settings...")
                val _repoUrl = event.prefs.getString(PREFS_VALUES.GIT_REPO_URL, "")
                val _isRepoCloned = event.prefs.getBoolean(PREFS_VALUES.IS_REPO_CLONED, false)
                gitRepoState =
                    gitRepoState.copy(gitRepoUrl = _repoUrl, isGitCloneLoaded = _isRepoCloned)
                Log.i("Git", "Settings loaded")
            }

            is GitRepoEvent.GitFetch -> {
                gitRepoState = gitRepoState.copy(isGitFetchPending = true)
                if (gitRepoState.isGitCloneLoaded && gitRepoState.gitRepoUrl != "") {
                    viewModelScope.launch {
                        gitFetch(
                            context = event.context,
                            repoUrl = gitRepoState.gitRepoUrl!!,
                            login = event.login,
                            password = event.password
                        )
                        gitRepoState = gitRepoState.copy(isGitFetched = true, isGitFetchPending = false)
                    }
                } else {
                    Log.i("Git", "Unable to fetch, theres no repository or repo url is invalid.")
                }
            }

            else -> {
                Log.e("Event handler", "No such event: $event")
            }
        }
    }
}
