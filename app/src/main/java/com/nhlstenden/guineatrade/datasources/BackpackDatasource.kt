package com.nhlstenden.guineatrade.datasources

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat.getString
import com.nhlstenden.guineatrade.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.Request
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class PriceCache(
    val cachedOn: String,
    val items: HashMap<String, Item>,
    @Contextual val timestamp: Instant = Instant.parse(cachedOn)
) {
    fun getSpecificPricing(itemHashName: String, quality: Quality, craftable: Boolean, unusual: String? = null): Int? {
        val itemPair: ItemPair? = items[itemHashName]?.prices[quality]
        if (itemPair != null) {
            return (if (craftable) itemPair.craftable else itemPair.uncraftable)[unusual ?: "0"]
        }
        return null
    }
}

@Serializable
data class Item(
    val icon: String,
    val defindex: List<Int>,
    val marketHashName: String,
    val prices: HashMap<Quality, ItemPair>
){
    fun getSpecificPricingData(quality: Quality, craftable: String): HashMap<String, Int>? {
        val itemPair = prices[quality]
        return if (craftable == "craftable") itemPair?.craftable else itemPair?.uncraftable
    }

    fun getSpecificPricingData(category: Category): HashMap<String, Int>? {
       return getSpecificPricingData(category.quality, category.craftable)
    }

    fun getCategories(): List<Category> {
        return prices.flatMap { (quality, itemPair) ->
            buildList {
                if (itemPair.craftable.isNotEmpty()) { add(Category(quality, "craftable")) }
                if (itemPair.uncraftable.isNotEmpty()) { add(Category(quality, "uncraftable")) }
            }
        }
    }
}

data class Category(
    val quality: Quality,
    val craftable: String
) {
    fun toName(): String {
        return "${quality.name} - $craftable"
    }
}

@Serializable
data class ItemPair(
    val craftable: HashMap<String, Int> = HashMap(),
    @SerialName("non-craftable")
    val uncraftable: HashMap<String, Int> = HashMap(),
)

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
        } catch (e: Exception) {
            Log.d("BackpackDatasource", e.message.toString())
            return@withContext false
        }

        return@withContext true
    }

    fun formatInstantToString(context: Context): String {
        if (this.prices == null) {
            return "loading..."
        }
        val zonedDateTime = this.prices!!.timestamp.atZone(ZoneId.systemDefault())
        val currentTime = LocalTime.of(zonedDateTime.hour, zonedDateTime.minute)

        val today = LocalDate.now()
        val format = DateTimeFormatter.ofPattern("HH:mm")

        val currentTimeString = currentTime.format(format)

        return when (val daysAgo = ChronoUnit.DAYS.between(today, zonedDateTime.toLocalDate())) {
            0L -> getString(context, R.string.backpack_time_format_today).format(currentTimeString)
            1L -> getString(context, R.string.backpack_time_format_today).format(currentTimeString)
            else -> getString(context, R.string.backpack_time_format_days_ago).format(daysAgo, currentTimeString)
        }
    }
}
