package com.example.webview.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.webview.PREFS_VALUES
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class AppEvent {
    data class LoadAppSettings(val prefs: SharedPreferences) : AppEvent()
    data class Login(val login: String, val password: String) : AppEvent()
}

data class AppState(
    val login: String = "",
    val password: String = "",
    val bearer: String = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcGkiOjEsImdycCI6MSwiaWF0IjoxNjk3MTY0OTA2LCJleHAiOjE3Mjg3MjI1MDYsImF1ZCI6InVybjp3aWtpLmpzIiwiaXNzIjoidXJuOndpa2kuanMifQ.HFAxu393tjFYz8Q6kIFe8aH0SBL7TLLaeSAhBj4XmUD5UjuhshlVQ9VqQIcQ4b8bqgD5W0_iVCgm-CziE6_0sJGUiO4rKeAuA3GGb7ltIJoa63XMYlrs5yU201iGmAJ5zWS87CIt5D4QaUtdF1CDjZXPwdStjftxe1z3YIWUeAT58yp4aSgRmR9l_bATl7sKnEX9B05vlC2WNFHCs1uiNdSULhJdZ0il9Ib95YqI99fS4a4OG_heaqEpYDPgYuLW0HDKUnqPsdCERwqKRtEhQKfuDsd3A-uFM8TmuAlrWfTrrX6yxu4ym5LMDF1lW2461ljUkQalfvRhRzYd0CKmpg"
)

@HiltViewModel
class AppViewModel @Inject constructor() : ViewModel() {

    var appState by mutableStateOf(AppState())
        private set

    fun onEvent(event: AppEvent) {
        when (event) {
            is AppEvent.LoadAppSettings -> {
                val login = event.prefs.getString(PREFS_VALUES.GITLAB_LOGIN, "")
                val pass = event.prefs.getString(PREFS_VALUES.GITLAB_PASS, "")
                val bearer = event.prefs.getString(PREFS_VALUES.WIKI_JS_BEARER, "")
                appState = appState.copy(
                    login = login!!,
                    password = pass!!,
//                    bearer = bearer!!
                )
            }
            is AppEvent.Login -> {
                appState = appState.copy(
                    login = event.login,
                    password = event.password
                )
            }
            else -> {
                Log.e("Event handler", "No such event: $event")
            }
        }
    }
}
