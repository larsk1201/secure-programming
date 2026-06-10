package com.nhlstenden.guineatrade.datasources

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class InventoryResponse(
    val success: Int,
    val descriptions: List<Description>,
    val assets: List<Asset>
)

@Serializable
data class Description(
    val name: String,
    val icon_url: String,
    val classid: String,
    val instanceid: String
)

@Serializable
data class Asset(
    val assetid: String,
    val classid: String,
    val instanceid: String
)

@Singleton
class InventoryDatasource @Inject constructor(
    private val userDatasource: UserDatasource
) {
    private val client = HttpClient()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getInventory(steamId: String): InventoryResponse? =
        withContext(Dispatchers.IO) {

            val request = okhttp3.Request.Builder()
                .url(client.userInventory(steamId))
                .get()
                .header(
                    "Authorization",
                    "Bearer ${userDatasource.tokens.jwt}"
                )
                .build()

            try {
                val response =
                    client.client.newCall(request).execute()

                if (response.code != 200) return@withContext null

                return@withContext json.decodeFromStream<InventoryResponse>(
                    response.body.byteStream()
                )

            } catch (e: Exception) {
                Log.d("InventoryDatasource", e.message.toString())
                return@withContext null
            }
        }
}