package com.nhlstenden.guineatrade.datasources

import android.util.Log
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
data class Tokens(var jwt: String, val refresh: String)

@Serializable
data class AuthMe(
    val email: String,
    val name: String,
    val tel: String,
    val balance: Int,
)

@Serializable
data class Login(val email: String, val password: String)

@Serializable
data class SignUp(
    val name: String,
    val email: String,
    val password: String,
    val passwordVerify: String,
    val tel: String,
)

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

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun authMe() = withContext(Dispatchers.IO) {
        if (this@UserDatasource.tokens == null) {
            return@withContext
        }
        val url = this@UserDatasource.client.apiUrl + "/auth/me"
        val request = Request.Builder()
            .url(url)
            .get()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + this@UserDatasource.tokens.jwt)
            .build()

        val result = this@UserDatasource.client.client.newCall(request).execute()

        val credentials = Json.decodeFromStream<AuthMe>(result.body.byteStream())

        this@UserDatasource.email = credentials.email
        this@UserDatasource.username = credentials.name
        this@UserDatasource.phone = credentials.tel
        this@UserDatasource.balance = credentials.balance
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun signup(name: String, email: String, password: String, passwordConfirm: String, phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        val url = this@UserDatasource.client.apiUrl + "/auth/register"
        val jsonBody = Json.encodeToString(SignUp(name, email, password,passwordConfirm, phoneNumber ))
        Log.d("UserDatasource", jsonBody)
        val body = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("Content-Type", "application/json")
            .build()

        val result = this@UserDatasource.client.client.newCall(request).execute()

        if (result.code != 201) {
            return@withContext false
        }

        try {
            this@UserDatasource.login(email, password)
        } catch (_: Exception) {
            return@withContext false
        }

        return@withContext true
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun refreshToken() = withContext(Dispatchers.IO) {
        val url = this@UserDatasource.client.apiUrl + "/auth/refresh"
        val jsonBody = Json.encodeToString(this@UserDatasource.tokens)
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
            val tokens = Json.decodeFromStream<Tokens>(result.body.byteStream())
            this@UserDatasource.tokens.jwt = tokens.jwt
        } catch (_: Exception) {
            return@withContext false
        }

        return@withContext true
    }

    @Provides
    @Singleton
    fun provideUser(): UserDatasource = UserDatasource()
}