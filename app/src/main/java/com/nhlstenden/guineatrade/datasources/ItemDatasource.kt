package com.nhlstenden.guineatrade.datasources

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.nhlstenden.guineatrade.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
enum class Quality {
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
    DECORATED;

    fun toColour(context: Context): Int {
        return when (this) {
            UNIQUE -> ContextCompat.getColor(context, R.color.tf_unique)
            VINTAGE -> ContextCompat.getColor(context, R.color.tf_vintage)
            GENUINE -> ContextCompat.getColor(context, R.color.tf_genuine)
            STRANGE -> ContextCompat.getColor(context, R.color.tf_strange)
            UNUSUAL -> ContextCompat.getColor(context, R.color.tf_unusual)
            HAUNTED -> ContextCompat.getColor(context, R.color.tf_haunted)
            COLLECTORS -> ContextCompat.getColor(context, R.color.tf_collectors)
            DECORATED -> ContextCompat.getColor(context, R.color.tf_decorated)
            COMMUNITY -> ContextCompat.getColor(context, R.color.tf_community)
            VALVE -> ContextCompat.getColor(context, R.color.tf_valve)
            else -> ContextCompat.getColor(context, R.color.tf_normal)
        }
    }

}

@Serializable
data class InventoryItem(
    val assetid: String,
    val instanceid: String,
    val classid: String,
    val defindex: Int,
    val marketHashName: String,
    val craftability: Boolean,
    val quality: Quality,
    val unusual: String? = null
)

data class Stock(
    val item: SpecificItem,
    var quantity: Int
)

data class SpecificItem(
    val marketHashName: String,
    val craftability: Boolean,
    val quality: Quality,
    val unusual: String? = "0",
)

@Serializable
data class StockResponse(
    val marketHashName: String,
    val craftability: Boolean,
    val quality: Quality,
    val unusual: String? = "0",
    var quantity: Int
) {
    fun toStock(): Stock {
        return Stock(SpecificItem(marketHashName, craftability, quality, unusual), quantity)
    }
}

@Singleton
class ItemDatasource @Inject constructor(
    private val userDatasource: UserDatasource
) {
    private val client = HttpClient()

    var userInventory: List<InventoryItem> = emptyList()
    var botInventory: List<InventoryItem> = emptyList()
    var userStock: List<Stock> = emptyList()
    var botStock: List<Stock> = emptyList()

    fun isInventoryEmpty(): Boolean {
        return this.botInventory.isEmpty() || this.userInventory.isEmpty()
    }

    suspend fun setupInventories(): Boolean = withContext(Dispatchers.IO) {
        return@withContext this@ItemDatasource.getInventory(false) &&
                this@ItemDatasource.getInventory(true)
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getInventory(isPlayerInventory: Boolean): Boolean = withContext(Dispatchers.IO) {
        val url = if (isPlayerInventory) {
            this@ItemDatasource.client.userInventory
        } else {
            this@ItemDatasource.client.steamInventory
        }
        val request = Request.Builder()
            .url(url)
            .get()
            .header("Authorization", "Bearer ${this@ItemDatasource.userDatasource.tokens.jwtSave}")
            .build()

        try {
            val result = this@ItemDatasource.client.client.newCall(request).execute()

            if (result.code != 200) {
                return@withContext false
            }

            val data = Json.decodeFromStream<List<InventoryItem>>(result.body.byteStream())
            if (isPlayerInventory) {
                this@ItemDatasource.userInventory = data
            } else {
                this@ItemDatasource.botInventory = data
            }
        } catch (e: Exception) {
            Log.d("ItemDatasource", e.message.toString())
            return@withContext false
        }

        return@withContext true
    }

    fun isStockEmpty(): Boolean {
        return this.botStock.isEmpty() || this.userStock.isEmpty()
    }

    suspend fun setupStock(): Boolean = withContext(Dispatchers.IO) {
        return@withContext this@ItemDatasource.getStock(true) &&
                this@ItemDatasource.getStock(false)
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getStock(isPlayerStock: Boolean): Boolean = withContext(Dispatchers.IO) {
        val url = if (isPlayerStock) {
            this@ItemDatasource.client.userStock
        } else {
            this@ItemDatasource.client.steamStock
        }

        val request = Request.Builder()
            .url(url)
            .get()
            .header("Authorization", "Bearer ${this@ItemDatasource.userDatasource.tokens.jwtSave}")
            .build()

        try {
            Log.d("ItemDatasource", "Attempting $isPlayerStock")
            val result = this@ItemDatasource.client.client.newCall(request).execute()

            if (result.code != 200) {
                return@withContext false
            }

            val data = Json.decodeFromStream<List<StockResponse>>(result.body.byteStream()).map { it.toStock() }

            Log.d("ItemDatasource", "data of $isPlayerStock: $data")
            if (isPlayerStock) {
                this@ItemDatasource.userStock = data
            } else {
                this@ItemDatasource.botStock = data
            }
        } catch (e: Exception) {
            Log.d("ItemDatasource", e.message.toString())
            return@withContext false
        }

        return@withContext true
    }
}