package com.nhlstenden.guineatrade.datasources

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class Tokens(val jwt: String, val refresh: String)

@Serializable
data class AuthMe(
    val email: String,
    val name: String,
    val tel: String,
    val balance: Int,
)

@Serializable
data class Login(val email: String, val password: String)

@Module
@InstallIn(SingletonComponent::class)
class UserDatasource @Inject constructor() {
    private val client = HttpClient()
    lateinit var username: String
    lateinit var email: String
    lateinit var phone: String
    var balance = 0
    lateinit var tokens: Tokens

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun login(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val url = this@UserDatasource.client.apiUrl + "/auth/login"
        val jsonBody = Json.encodeToString(Login(email, password))
        val body = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("Content-Type", "application/json")
            .build()

        val result = this@UserDatasource.client.client.newCall(request).execute()

        if (result.code != 200) {
            return@withContext false
        }

        try {
            this@UserDatasource.tokens = Json.decodeFromStream<Tokens>(result.body.byteStream())
            this@UserDatasource.authMe()
        } catch (_: Exception) {
            return@withContext false
        }

        return@withContext true
    }



    fun signup(email: String, password: String, passwordConfirm: String, phoneNumber: String): Boolean {
        this.username = email
        this.email = email
        this.phone = phoneNumber
//        TODO: Make API call

        return true
    }

    @Provides
    @Singleton
    fun provideUser(): UserDatasource = UserDatasource()
}