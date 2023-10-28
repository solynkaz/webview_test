package com.example.webview.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.webview.viewmodel.AppEvent
import com.example.webview.viewmodel.AppViewModel
import com.example.webview.viewmodel.GitRepoEvent
import com.example.webview.viewmodel.GitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginCompose(
    isThereNetworkConnection: MutableState<Boolean>,
    appViewModel: AppViewModel = hiltViewModel(),
    gitViewModel: GitViewModel = hiltViewModel()
) {
    val login = remember { mutableStateOf("zma@sibdigital.net") }
    val password = remember { mutableStateOf("J0tul8878q1e3") }
    val context = LocalContext.current
    val textFieldModifier = Modifier.fillMaxWidth()
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.weight(1f))
        Text("Git Авторизация", modifier = Modifier.align(Alignment.CenterHorizontally), fontSize = 24.sp)
        Row() {
            Spacer(Modifier.weight(0.5f))
            OutlinedTextField(
                modifier = textFieldModifier.weight(1f),
                singleLine = true,
                label = {
                    Text(text = "Логин")
                },
                value = login.value,
                onValueChange = { letter -> login.value = letter }
            )
            Spacer(Modifier.weight(0.5f))
        }
        Row() {
            Spacer(Modifier.weight(0.5f))
            OutlinedTextField(
                modifier = textFieldModifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                label = {
                    Text(text = "Пароль")
                },
                value = password.value,
                onValueChange = { letter -> password.value = letter }
            )
            Spacer(Modifier.weight(0.5f))
        }
        Row() {
            Spacer(Modifier.weight(0.5f))
            Button(
                onClick = {
                    appViewModel.onEvent(AppEvent.Login(login.value, password.value))
                    if (!gitViewModel.gitRepoState.isGitUpdatePending && isThereNetworkConnection.value) {
                        gitViewModel.onEvent(
                            GitRepoEvent.GitClone(
                                context = context,
                                login = appViewModel.appState.login,
                                password = appViewModel.appState.password,
                                urlRepo = gitViewModel.gitRepoState.gitRepoUrl,
                                appViewModel = appViewModel
                            )
                        )
                    }
                }, modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f)
                    .padding(top = 15.dp)
            ) {
                Text("Войти")
            }
            Spacer(Modifier.weight(0.5f))
        }
        Spacer(Modifier.weight(1f))
    }
}
