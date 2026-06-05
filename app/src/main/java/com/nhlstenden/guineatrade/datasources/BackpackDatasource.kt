package com.nhlstenden.guineatrade.datasources

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class PriceCache(
    val cachedOn: String,
    val items: HashMap<String, Item>,
    @Contextual val timestamp: Instant = Instant.parse(cachedOn)
)

@Serializable
data class Item(
    val icon: String,
    val defindex: List<Int>,
    val prices: HashMap<Qualty, ItemPair>
)

@Serializable
data class ItemPair(
    val craftable: HashMap<String, Int>,
    val uncraftable: HashMap<String, Int>,
)

@Serializable
enum class Qualty {
    @SerialName("Normal")
    NORMAL,

    @SerialName("Genuine")
    GENUINE,

    @SerialName("Rarity2")
    RARITY_2,

    @SerialName("Vintage")
    VINTAGE,

    @SerialName("Rarity3")
    RARITY_3,

    @SerialName("Unusual")
    UNUSUAL,

    @SerialName("Unique")
    UNIQUE,

    @SerialName("Community")
    COMMUNITY,

    @SerialName("Valve")
    VALVE,

    @SerialName("Self-Made")
    SELF_MADE,

    @SerialName("Customized")
    CUSTOMIZED,

    @SerialName("Strange")
    STRANGE,

    @SerialName("Completed")
    COMPLETED,

    @SerialName("Haunted")
    HAUNTED,

    @SerialName("Collectors")
    COLLECTORS,

    @SerialName("Decorated")
    DECORATED,
}

@Singleton
class BackpackDatasource @Inject constructor() {
    private val client = HttpClient()

    var prices: PriceCache? = null

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getPrices(jwt: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(this@BackpackDatasource.client.backpackPrices)
            .get()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $jwt")
            .build()

        try {
            val result = this@BackpackDatasource.client.client.newCall(request).execute()

            if (result.code != 200) {
                return@withContext false
            }

            this@BackpackDatasource.prices = Json.decodeFromStream<PriceCache>(result.body.byteStream())
        } catch (_: Exception) {
            return@withContext false
        }

        return@withContext true
    }
}