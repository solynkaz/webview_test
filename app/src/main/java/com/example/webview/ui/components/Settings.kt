package com.example.webview.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.example.webview.viewmodel.AppViewModel
import com.example.webview.viewmodel.GitViewModel

@Composable
fun Settings_Screen(
    isThereNetworkConnection: MutableState<Boolean>,
    gitViewModel: GitViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    innerPaddingValues: PaddingValues
) {
    val wikiJSBearer = remember { mutableStateOf("") }
    val gitLogin = remember { mutableStateOf("") }
    val gitPassword = remember { mutableStateOf("") }
    val context = LocalContext.current
    val defaultPadding = PaddingValues(start = 15.dp, top = 10.dp)
    Column(Modifier.fillMaxSize().padding(innerPaddingValues)) {
        Text("Настройки", modifier = Modifier.padding(defaultPadding), fontSize = 24.sp)
        Text("${isThereNetworkConnection.value}", modifier = Modifier.padding(defaultPadding), fontSize = 24.sp)
        CredentialsCompose(
            defaultPadding = defaultPadding,
            gitLogin = gitLogin,
            gitPassword = gitPassword,
            bearer = wikiJSBearer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialsCompose(
    defaultPadding: PaddingValues,
    gitLogin: MutableState<String>,
    gitPassword: MutableState<String>,
    bearer: MutableState<String>,
) {
    val textFieldModifier = Modifier
        .fillMaxWidth()
        .padding(defaultPadding)
        .padding(end = 15.dp)

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
        Button(onClick = {

        }, modifier = Modifier
            .padding(top = 15.dp)
            .align(Alignment.CenterHorizontally)) {
            Text("Сохранить")
        }
    }
}