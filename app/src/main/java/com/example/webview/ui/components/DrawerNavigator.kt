package com.example.webview.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.webview.AppConsts
import com.example.webview.R
import com.example.webview.viewmodel.AppEvent
import com.example.webview.viewmodel.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File


val dividerModifier = Modifier.height(2.dp).padding(horizontal = 12.dp)
val notToDisplayFolders = listOf(".git")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryNavigation(
    drawerState: DrawerState,
    coroutineScope: CoroutineScope,
    appViewModel: AppViewModel
) {
    val currentDirectory by rememberUpdatedState(newValue = appViewModel.appState.currentDirectory)
    val breadCrumbs = remember { mutableListOf(currentDirectory) }
    LaunchedEffect(currentDirectory) {
        if (currentDirectory != null) {
            var _currentDirectory = currentDirectory
            breadCrumbs.clear()
            while (_currentDirectory?.name != AppConsts.GIT_FOLDER) {
                breadCrumbs.add(0, _currentDirectory)
                _currentDirectory = _currentDirectory?.parentFile
            }
            breadCrumbs.add(0, _currentDirectory)
        }
    }
    val context = LocalContext.current
    Divider()
    LazyRow(
        contentPadding = PaddingValues(horizontal = 6.dp),
        modifier = Modifier.padding(vertical = 10.dp)
    ) {
        items(breadCrumbs) { crumb ->
            if (crumb != null) {
                Text(
                    crumb.name,
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.clickable {
                        appViewModel.onEvent(AppEvent.ChangeDirectory(crumb))
                        updateCrumbs(crumb, breadCrumbs)
                    })
            }
            Text(" / ", fontSize = 18.sp)
        }
    }
    Divider()
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        content = {
            items(currentDirectory?.listFiles().orEmpty().sortedBy { !it.isDirectory }) { file ->
                when {
                    file.isDirectory && !notToDisplayFolders.contains(file.name) -> {
                        ListItem(
                            modifier = Modifier.clickable {
                                appViewModel.onEvent(
                                    AppEvent.ChangeDirectory(
                                        file
                                    )
                                )
                                updateCrumbs(file, breadCrumbs)
                            },
                            headlineText = { Text(text = file.name) },
                            trailingContent = {
                                Icon(
                                    painter = painterResource(id = R.drawable.folder),
                                    contentDescription = null
                                )
                            },
                        )
                        Divider(dividerModifier)
                    }
                    file.isFile -> {
                        if (file.extension == "md" || file.extension == "html") {
                            ListItem(
                                modifier = Modifier.clickable {
                                    coroutineScope.launch {
                                        drawerState.close()
                                        appViewModel.onEvent(
                                            AppEvent.LoadLocalFile(
                                                currentLocalFile = file,
                                                context = context
                                            )
                                        )
                                    }
                                },
                                headlineText = { Text(text = file.name) },
                                trailingContent = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.file),
                                        contentDescription = null
                                    )
                                },
                            )
                            Divider(dividerModifier)
                        }
                    }
                }
            }
        })


}
fun updateCrumbs(file: File, breadCrumbs: MutableList<File?>) {
    val index = breadCrumbs.indexOf(file)
    if (index != -1) {
        // Remove all breadcrumbs after the target directory
        breadCrumbs.subList(index + 1, breadCrumbs.size).clear()
    }
}