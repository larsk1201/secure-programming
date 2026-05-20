package com.nhlstenden.guineatrade.datasources

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

data class HttpClient(
    val client: OkHttpClient = OkHttpClient(),
    val apiUrl: String = "https://secprog.infinite-night.com/api/v1"
)

