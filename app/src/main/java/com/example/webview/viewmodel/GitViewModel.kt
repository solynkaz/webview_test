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
import com.example.webview.PREFS_VALUES.PREFS
import com.example.webview.controller.getRepoURL
import com.example.webview.controller.gitClone
import com.example.webview.controller.gitFetch
import com.example.webview.controller.isRepoEmpty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GitRepoEvent {
    data class GitClone(
        val context: Context,
        val login: String,
        val password: String,
        val urlRepo: String
    ) : GitRepoEvent()

    data class GitFetch(
        val context: Context,
        val login: String,
        val password: String
    ): GitRepoEvent()
    data class GetRepoUrl(
        val context: Context,
        val bearer: String
    ): GitRepoEvent()

    data class LoadGitSettings(val prefs: SharedPreferences) : GitRepoEvent()
}

data class GitRepoState(
    val gitRepoUrl: String = "",
    val isGetRepoUrlPending: Boolean = false,
    val isGitUpdated: Boolean = false,
    val isGitUpdatePending: Boolean = false,

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
                gitRepoState = gitRepoState.copy(isGitUpdatePending = true,isGetRepoUrlPending = true)
                viewModelScope.launch {
                    //Get repo url
                    Log.i("Git", "Started getting repo url...")
                    getRepoURL(event.bearer, event.context)
                    val pref: SharedPreferences = event.context.getSharedPreferences(
                        PREFS,
                        ComponentActivity.MODE_PRIVATE
                    )
                    gitRepoState = gitRepoState.copy(
                        isGetRepoUrlPending = false,
                        gitRepoUrl = pref.getString(PREFS_VALUES.GIT_REPO_URL, "")!!
                    )
                    Log.i(
                        "Git",
                        "Repo url updated: ${pref.getString(PREFS_VALUES.GIT_REPO_URL, "")}"
                    )
                }
            }
            is GitRepoEvent.GitFetch -> {
                viewModelScope.launch {
                    doGitFetch(event.context, event.login, event.password)
                }
            }
            is GitRepoEvent.GitClone -> {
                viewModelScope.launch {
                    doGitClone(context = event.context, login = event.login, password = event.password, gitRepoUrl = event.urlRepo)
                }
            }
            is GitRepoEvent.LoadGitSettings -> {
                Log.i("Git", "Loading settings...")
                val repoUrl = event.prefs.getString(PREFS_VALUES.GIT_REPO_URL, "")
                Log.i("Git", "Putting $repoUrl into state")
                gitRepoState =
                    gitRepoState.copy(gitRepoUrl = repoUrl!!)
                Log.i("Git", "Settings loaded")
            }
        }
    }

    private suspend fun doGitClone(context: Context, login: String, gitRepoUrl: String, password: String) {
        if (isRepoEmpty(context)) {
            Log.i("Git", "Start cloning repository with $login : $password by URL $gitRepoUrl")
            gitRepoState = gitRepoState.copy(isGitClonePending = true)
            val response = gitClone(
                context = context,
                repoUrl = gitRepoUrl,
                login = login,
                password = password
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
                    context,
                    "Error while git clone",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.i("Git", "Error while git clone")
                gitRepoState = gitRepoState.copy(isGitClonePending = false)
            }
        } else {
            gitRepoState = gitRepoState.copy(isGitCloneLoaded = true)
            Log.i("Git", "Git repository already exists, clone was not called")
        }
    }

    private suspend fun doGitFetch(context: Context, login: String, password: String) {
        gitRepoState = gitRepoState.copy(isGitFetchPending = true)
        if (gitRepoState.isGitCloneLoaded && gitRepoState.gitRepoUrl != "") {
            gitFetch(
                context = context,
                repoUrl = gitRepoState.gitRepoUrl!!,
                login = login,
                password = password
            )
            gitRepoState =
                gitRepoState.copy(isGitFetched = true, isGitFetchPending = false)
        } else {
            Log.i(
                "Git",
                "Unable to fetch, theres no repository or repo url is invalid."
            )
        }
        gitRepoState = gitRepoState.copy(isGitUpdated = true)
    }
}