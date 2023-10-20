package com.example.webview.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object GraphQLInstance {

    private const val BASE_URL: String = "http://192.168.0.34:3000/"

    val graphQLService: GraphQLService by lazy {
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient().newBuilder()
//                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build()
            )
            .addConverterFactory(ScalarsConverterFactory.create())
            .build().create(GraphQLService::class.java)
    }
}