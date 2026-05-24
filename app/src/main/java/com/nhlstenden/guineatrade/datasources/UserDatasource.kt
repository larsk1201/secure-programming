package com.nhlstenden.guineatrade.datasources

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class Tokens(var jwt: String, val refresh: String)

@Serializable
data class AuthMe(
    val email: String,
    val name: String,
    val balance: Int,
    val mfaEnabled: Boolean
)

@Serializable
data class Login(val email: String, val password: String)

@Serializable
data class SignUp(
    val name: String,
    val email: String,
    val password: String,
    val passwordVerify: String,
)

@Serializable
data class UpdateMe(
    val email: String,
    val currentPassword: String,
    val newPassword: String,
    val newPasswordVerify: String,
){}

@Serializable
data class TotpTokens(
    val code: String,
    val recovery: String,
)

@Module
@InstallIn(SingletonComponent::class)
class UserDatasource @Inject constructor() {
    private val client = HttpClient()

    private var _username: MutableLiveData<String> = MutableLiveData()
    private var _email: MutableLiveData<String> = MutableLiveData()
    private var _hasMFA: MutableLiveData<Boolean> = MutableLiveData(false)
    private var _balance: MutableLiveData<Int> = MutableLiveData(0)
    private var _tokens: MutableLiveData<Tokens> = MutableLiveData()

    val usernameLiveData: LiveData<String> get() = _username
    val emailLiveData: LiveData<String> get() = _email
    val hasMFALiveData: LiveData<Boolean> get() = _hasMFA
    val balanceLiveData: LiveData<Int> get() = _balance
    val tokensLiveData: LiveData<Tokens> get() = _tokens

    var username: String
        get() = _username.value ?: ""
        set(value) { _username.value = value }
    var email: String
        get() = _email.value ?: ""
        set(value) { _email.value = value }
    var hasMFA: Boolean
        get() = _hasMFA.value ?: false
        set(value) { _hasMFA.value = value }
    var balance: Int
        get() = _balance.value ?: 0
        set(value) { _balance.value = value }
    var tokens: Tokens
        get() = _tokens.value ?: Tokens("", "")
        set(value) { _tokens.value = value}


    @OptIn(ExperimentalSerializationApi::class)
    suspend fun login(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val jsonBody = Json.encodeToString(Login(email, password))
        val body = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(this@UserDatasource.client.authLogin)
            .post(body)
            .header("Content-Type", "application/json")
            .build()

        try {
            val result = this@UserDatasource.client.client.newCall(request).execute()

            if (result.code != 200) {
                return@withContext false
            }

            this@UserDatasource.tokens = Json.decodeFromStream<Tokens>(result.body.byteStream())
            this@UserDatasource.authMe()
        } catch (_: Exception) {
            return@withContext false
        }

        return@withContext true
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun authMe() = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(this@UserDatasource.client.authMe)
            .get()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + this@UserDatasource.tokens.jwt)
            .build()

        val result = this@UserDatasource.client.client.newCall(request).execute()

        val credentials = Json.decodeFromStream<AuthMe>(result.body.byteStream())

        this@UserDatasource.email = credentials.email
        this@UserDatasource.username = credentials.name
        this@UserDatasource.balance = credentials.balance
        this@UserDatasource.hasMFA = credentials.mfaEnabled
    }

    suspend fun updateMe(totpCode: String, currentPassword: String, newPassword: String, newPasswordVerify: String): Boolean = withContext(Dispatchers.IO) {
        val jsonBody = Json.encodeToString(UpdateMe(this@UserDatasource.email,
            currentPassword,
            newPassword,
            newPasswordVerify,
        ))
        val body = jsonBody.toRequestBody("application/json".toMediaType())
        val requestBuilder = Request.Builder()
            .url(this@UserDatasource.client.authMe)
            .patch(body!!)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${this@UserDatasource.tokens.jwt}")

        if (totpCode != "") {
            requestBuilder.header("X-TOTP-Code", totpCode)
        }

        try {
            val result = this@UserDatasource.client.client.newCall(requestBuilder.build()).execute()

            if (result.code == 202) {
                return@withContext true
            }
        } catch (_: Exception) {
            return@withContext false
        }

        return@withContext false
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun signup(name: String, email: String, password: String, passwordConfirm: String): Boolean = withContext(Dispatchers.IO) {
        val jsonBody = Json.encodeToString(SignUp(name, email, password,passwordConfirm))
        val body = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(this@UserDatasource.client.authRegister)
            .post(body)
            .header("Content-Type", "application/json")
            .build()

        try {
            val result = this@UserDatasource.client.client.newCall(request).execute()

            if (result.code != 201) {
                return@withContext false
            }

            this@UserDatasource.login(email, password)
        } catch (_: Exception) {
            return@withContext false
        }

        return@withContext true
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun refreshToken() = withContext(Dispatchers.IO) {
        val jsonBody = Json.encodeToString(this@UserDatasource.tokens)
        val body = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(this@UserDatasource.client.authRefresh)
            .post(body)
            .header("Content-Type", "application/json")
            .build()

        val result = this@UserDatasource.client.client.newCall(request).execute()

        if (result.code != 200) {
            return@withContext false
        }

        try {
            this@UserDatasource.tokens = Json.decodeFromStream<Tokens>(result.body.byteStream())
        } catch (_: Exception) {
            return@withContext false
        }

        return@withContext true
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun registerTOTPToken(): Result<TotpTokens> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(this@UserDatasource.client.auth2fATotpRegister)
            .post(RequestBody.EMPTY)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${this@UserDatasource.tokens.jwt}")
            .build()

        try {
            val result = this@UserDatasource.client.client.newCall(request).execute()

            if (result.code != 200) {
                return@withContext Result.failure(Exception("Failed to register TOTP"))
            }

            val tokens = Json.decodeFromStream<TotpTokens>(result.body.byteStream())

            Log.d("UserDatasource", tokens.code)
            Log.d("UserDatasource", tokens.recovery)

            return@withContext Result.success(tokens)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun validateTOTPCode(code: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(this@UserDatasource.client.auth2fATotpVerify)
            .post(RequestBody.EMPTY)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${this@UserDatasource.tokens.jwt}")
            .header("X-TOTP-Code", code)
            .build()

        try {
            val result = this@UserDatasource.client.client.newCall(request).execute()

            if (result.code != 200) {
                return@withContext false
            }

        } catch (_: Exception) {
            return@withContext false
        }

        return@withContext true
    }

    suspend fun deactivateTOTPCode(code: String, isRecoveryCode: Boolean): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(this@UserDatasource.client.auth2fATotpReset)
            .delete(RequestBody.EMPTY)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${this@UserDatasource.tokens.jwt}")
        if (!isRecoveryCode) {
            request.header("X-TOTP-Code", code)
        } else {
            request.header("X-Recovery-Code", code)
        }

        try {
            val result = this@UserDatasource.client.client.newCall(request.build()).execute()

            if (result.code != 204) {
                return@withContext false
            }

        } catch (_: Exception) {
            return@withContext false
        }

        return@withContext true
    }

    @Provides
    @Singleton
    fun provideUser(): UserDatasource = UserDatasource()
}