package com.nhlstenden.guineatrade.datasources

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.utils.Pricing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
import kotlin.math.absoluteValue

// Sell = User sells item to us
// Buy = User buys item from us
enum class CartItemType {
    BUY,
    SELL,
}

data class Price (
    val price: Double,
    val type: CartItemType
) {
    fun toRealPrice(): Double {
        if (type == CartItemType.SELL) {
            return -1.0 * price
        }
        return price
    }
}


enum class ButtonColor {
    OUT_OF_STOCK,
    HAS_STOCK;

    fun toColour(context: Context): Int {
        return when (this) {
            OUT_OF_STOCK -> ContextCompat.getColor(context, R.color.gray)
            HAS_STOCK -> ContextCompat.getColor(context, R.color.purple_700)
        }
    }
}

data class CartItem(
    val stock: Stock,
    val imageUrl: String,
    var type: CartItemType = CartItemType.BUY
) {
    fun getPrice(backpackDatasource: BackpackDatasource): Price {
        val item = stock.item
        val price = backpackDatasource.prices
            ?.getSpecificPricing(item.marketHashName, item.quality, item.craftability, item.unusual)
            ?.toDouble()
            ?.div(100)
            ?.times(stock.quantity)
            ?.times((if (type == CartItemType.SELL) Pricing.SELL_MODIFIER else Pricing.BUY_MODIFIER))
            ?: 0.0

        return Price(price, type)
    }

    fun toData(): CartItemData {
        val item = stock.item
        return CartItemData(
            item.marketHashName,
            item.craftability,
            item.quality,
            item.unusual ?: "0",
            stock.quantity,
            type == CartItemType.SELL
        )
    }
}

@Serializable
data class CartItemData (
    val marketHashName: String,
    val craftability: Boolean,
    val quality: Quality,
    val unusual: String,
    val quantity: Int,
    val isSold: Boolean,
)

@Singleton
class CartDatasource @Inject constructor(){
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart

    private val client = HttpClient()

    @Inject
    lateinit var backpackDatasource: BackpackDatasource
    @Inject
    lateinit var itemDatasource: ItemDatasource
    @Inject
    lateinit var userDatasource: UserDatasource


    fun getSpecificStock(stock: List<Stock>, marketHashName: String, quality: Quality, isCraftable: Boolean, effect: String = "0"): Stock? {
        return stock.find {
            val stockItem = it.item
            stockItem.marketHashName == marketHashName
                    && stockItem.quality == quality
                    && stockItem.craftability == isCraftable
                    && stockItem.unusual == effect
        }
    }

    fun getSpecificBotStock(marketHashName: String, quality: Quality, isCraftable: Boolean, effect: String = "0"): Stock? {
        return getSpecificStock(itemDatasource.botStock, marketHashName, quality, isCraftable, effect)
    }

    fun getSpecificUserStock(marketHashName: String, quality: Quality, isCraftable: Boolean, effect: String = "0"): Stock? {
        return getSpecificStock(itemDatasource.userStock, marketHashName, quality, isCraftable, effect)
    }

    fun addItem(item: SpecificItem, type: CartItemType, imageUrl: String = "") {
        val current = _cart.value.toMutableList()

        val existing = current.find {
            it.stock.item == item
        }

        if (existing != null) {
            existing.stock.quantity++
        } else {
            current.add(CartItem(Stock(item, 1), imageUrl, type))
        }

        _cart.value = current
    }

    fun removeItem(item: SpecificItem) {
        val current = _cart.value.toMutableList()

        val existing = current.find {
            it.stock.item == item
        }

        if (existing != null) {
            if (existing.stock.quantity > 1) {
                existing.stock.quantity--
            } else {
                current.remove(existing)
            }
        }

        _cart.value = current
    }

    fun clearItem(item: SpecificItem) {
        _cart.value = _cart.value.filter { it.stock.item != item }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun getTotalPrice(): Price {
        val price = _cart.value.sumOf { it.getPrice(backpackDatasource).toRealPrice() }

        return Price(price.absoluteValue, if (price > 0.0) CartItemType.BUY else CartItemType.SELL)
    }

    @Serializable
    data class PaymentResponse(
        val status: String,
        val url: String? = null,
        val receives: Long? = null,
    )

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun createPaymentRequest(): PaymentResponse? = withContext(Dispatchers.IO) {
        val body = Json.encodeToString(cart.value.map { it.toData() }).toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(this@CartDatasource.client.paymentCreate)
            .post(body)
            .header("Authorization", "Bearer ${this@CartDatasource.userDatasource.tokens.jwtSave}")
            .build()

        try {
            val result = this@CartDatasource.client.client.newCall(request).execute()

            if (result.code != 200) {
                Log.d("CartDatasource", result.toString())
                return@withContext null
            }

            val data = Json.decodeFromStream<PaymentResponse>(result.body.byteStream())

            return@withContext data
        } catch (e: Exception) {
            Log.d("CartDatasource", e.message.toString())
            return@withContext null
        }
    }

    @Serializable
    data class TradeStatusResponse(
        val status: Int,
        val data: String
    )

    enum class TradeStatusType(val value: Int) {
        NO_ACTIVE_TRADE(-1),
        PAYMENT_IN_PROGRESS(0),
        TRADE_IN_PROGRESS(1),
        COMPLETED(3),
        CANCELLED(4);

        companion object {
            fun fromValue(value: Int): TradeStatusType? {
                return entries.firstOrNull { it.value == value }
            }
        }
    }

    data class TradeStatus(
        val status: TradeStatusType?,
        val data: String
    )

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getTradeStatus(): TradeStatus? = withContext(Dispatchers.IO) {
        val url = this@CartDatasource.client.tradeStatus

        val request = Request.Builder()
            .url(url)
            .get()
            .header("Authorization", "Bearer ${this@CartDatasource.userDatasource.tokens.jwtSave}")
            .build()

        try {
            val result = this@CartDatasource.client.client.newCall(request).execute()

            if (result.code != 200) {
                return@withContext null
            }

            val data = Json.decodeFromStream<TradeStatusResponse>(result.body.byteStream())
            return@withContext TradeStatus(TradeStatusType.fromValue(data.status), data.data)
        } catch (e: Exception) {
            Log.d("CartDatasource", e.message.toString())
            return@withContext null
        }
    }
}
