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

// Sell = User sells item to us
// Buy = User buys item from us
enum class CartItemType {
    BUY,
    SELL,
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
    fun getPrice(backpackDatasource: BackpackDatasource): Double {
        val item = stock.item
        return backpackDatasource.prices?.getSpecificPricing(item.marketHashName, item.quality, item.craftability, item.unusual)
            ?.div(100)?.times(stock.quantity)?.times((if (type == CartItemType.SELL) Pricing.SELL_MODIFIER else Pricing.BUY_MODIFIER)) ?: 0.0
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

    fun addItem(item: SpecificItem, type: CartItemType) {
        val current = _cart.value.toMutableList()

        val existing = current.find {
            it.stock.item == item
        }

        if (existing != null) {
            existing.stock.quantity++
        } else {
            current.add(CartItem(Stock(item, 1), "", type))
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

    fun getTotalPrice(): Double {
        return _cart.value.sumOf { it.getPrice(backpackDatasource) }
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
                return@withContext null
            }

            val data = Json.decodeFromStream<PaymentResponse>(result.body.byteStream())

            return@withContext data
        } catch (e: Exception) {
            Log.d("ItemDatasource", e.message.toString())
            return@withContext null
        }
    }
}
