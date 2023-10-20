package com.example.webview.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface GraphQLService {
    @Headers("Content-Type: application/json")
    @POST("/graphql")
    suspend fun postQuery(
        @Header("Authorization") bearer: String,
        @Body body: String): Response<String>
}