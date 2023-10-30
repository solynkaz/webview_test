package com.example.webview.ui

import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.webview.PREFS_VALUES
import com.example.webview.PREFS_VALUES.PREFS
import com.example.webview.controller.clearGitRepo
import com.example.webview.ui.components.ButtonCompose
import com.example.webview.ui.components.DividerCompose
import com.example.webview.viewmodel.AppEvent
import com.example.webview.viewmodel.AppViewModel
import com.example.webview.viewmodel.GitViewModel

@Composable
fun Settings_Screen(
    isThereNetworkConnection: MutableState<Boolean>,
    innerPaddingValues: PaddingValues
) {
    val wikiJSBearer = remember { mutableStateOf("") }
    val gitLogin = remember { mutableStateOf("") }
    val gitPassword = remember { mutableStateOf("") }
    val context = LocalContext.current
    val pref: SharedPreferences = context.getSharedPreferences(
        PREFS,
        ComponentActivity.MODE_PRIVATE
    )
    LaunchedEffect(Unit) {
        gitLogin.value = pref.getString(PREFS_VALUES.GIT_LOGIN, "")!!
        gitPassword.value = pref.getString(PREFS_VALUES.GIT_PASS, "")!!
        wikiJSBearer.value = pref.getString(PREFS_VALUES.WIKI_JS_BEARER, "")!!
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(innerPaddingValues)
    ) {
        CredentialsCompose(
            gitLogin = gitLogin,
            gitPassword = gitPassword,
            bearer = wikiJSBearer,
            pref = pref,
            isThereNetworkConnection = isThereNetworkConnection.value
        )
    }
}

val defaultPadding = PaddingValues(start = 15.dp, top = 10.dp)
val textFieldModifier = Modifier
    .fillMaxWidth()
    .padding(defaultPadding)
    .padding(end = 15.dp)
val buttonModifier = Modifier
    .padding(start = 15.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialsCompose(
    isThereNetworkConnection: Boolean,
    gitLogin: MutableState<String>,
    gitPassword: MutableState<String>,
    bearer: MutableState<String>,
    appViewModel: AppViewModel = hiltViewModel(),
    pref: SharedPreferences,
    gitViewModel: GitViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        DividerCompose(
            Modifier
                .padding(defaultPadding)
                .padding(end = 15.dp)
                .fillMaxWidth()
        )
        Text("Репозиторий", modifier = Modifier.padding(defaultPadding), fontSize = 18.sp)
        OutlinedTextField(
            modifier = textFieldModifier
                .align(Alignment.Start),
            singleLine = true,
            label = {
                Text(text = "Логин")
            },
            value = gitLogin.value,
            onValueChange = { letter -> gitLogin.value = letter }
        )
        OutlinedTextField(
            modifier = textFieldModifier,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            label = {
                Text(text = "Пароль")
            },
            value = gitPassword.value,
            onValueChange = { letter -> gitPassword.value = letter }
        )
        DividerCompose(
            Modifier
                .padding(defaultPadding)
                .padding(end = 15.dp)
                .fillMaxWidth()
        )
        Text("Wiki JS", modifier = Modifier.padding(defaultPadding), fontSize = 18.sp)
        OutlinedTextField(
            modifier = textFieldModifier,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            label = {
                Text(text = "API Ключ")
            },
            value = bearer.value,
            onValueChange = { letter -> bearer.value = letter }
        )
        Column(
            horizontalAlignment = Alignment.End, modifier = Modifier
                .fillMaxWidth()
                .padding(end = 15.dp)
        ) {
            // Сохранить
            ButtonCompose(
                onClick = {
                    val settingsMap = mapOf(
                        PREFS_VALUES.GIT_LOGIN to gitLogin.value,
                        PREFS_VALUES.GIT_PASS to gitPassword.value,
                        PREFS_VALUES.WIKI_JS_BEARER to bearer.value
                    )
                    appViewModel.onEvent(
                        AppEvent.SaveSettings(
                            settingsMap = settingsMap,
                            context = context
                        )
                    )
                }, modifier = buttonModifier.padding(top = 15.dp), label = "Сохранить", enabled = true
            )
            // Сохранить
            ButtonCompose(
                onClick = {
                    clearGitRepo(context)
                }, modifier = buttonModifier, label = "Очистить кэш", enabled = true
            )
        }
    }
}