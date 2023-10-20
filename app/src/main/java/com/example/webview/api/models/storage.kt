package com.example.webview.api.models


data class root (
    val data: data? = null
)
data class data(
    val storage: storage? = null
)

data class storage(
    val targets: List<target>? = null
)

data class target(
    val title: String? = null,
    val config: List<config>? = null
)

data class config(
    val key: String? = null,
    val value: String? = null
)