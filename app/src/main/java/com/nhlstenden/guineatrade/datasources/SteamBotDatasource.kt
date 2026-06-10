package com.nhlstenden.guineatrade.datasources

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class Inventory(
    val assetId: String,
    val marketHashName: String,
    val name: String,
    val tradable: Boolean,
    val marketable: Boolean
)

@Serializable
data class SteamInventoryData(
    val appId: Int,
    val contextId: Int,
    val count: Int,
    val inventory: List<Inventory>
)

@Singleton
class SteamBotDatasource @Inject constructor() {
    private val client = HttpClient()

    var inventoryData: HashMap<String, Int> = HashMap()

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getInventoryData(jwt: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(this@SteamBotDatasource.client.steamInventory)
            .get()
            .header("Authorization", "Bearer $jwt")
            .build()

        try {
            val result = this@SteamBotDatasource.client.client.newCall(request).execute()

            if (result.code != 200) {
                return@withContext false
            }

            val data = Json.decodeFromStream<SteamInventoryData>(result.body.byteStream())
            for (item in data.inventory) {
                if (!item.tradable) {
                    continue
                }

                inventoryData[item.marketHashName] = 1 + inventoryData.getOrDefault(item.marketHashName, 0)
            }
            Log.d("SteamBotDatasource", inventoryData[inventoryData.keys.first()].toString())
        } catch (e: Exception) {
            Log.d("BackpackDatasource", e.message.toString())
            return@withContext false
        }

        return@withContext true
    }
}