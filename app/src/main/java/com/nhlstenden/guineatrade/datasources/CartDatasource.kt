package com.nhlstenden.guineatrade.datasources

import android.content.Context
import androidx.core.content.ContextCompat
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.utils.Pricing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val items: List<InventoryItem>,
    val imageUrl: String,
    var type: CartItemType = CartItemType.BUY
) {
    fun getPrice(backpackDatasource: BackpackDatasource): Double {
        return backpackDatasource.prices?.getSpecificPricing(items[0])
            ?.div(100)?.times(items.size)?.times((if (type == CartItemType.SELL) Pricing.SELL_MODIFIER else Pricing.BUY_MODIFIER)) ?: 0.0
    }

    fun getFirst(): InventoryItem {
        return items[0]
    }
}

@Singleton
class CartDatasource @Inject constructor(){
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart

    @Inject
    lateinit var backpackDatasource: BackpackDatasource
    @Inject
    lateinit var itemDatasource: ItemDatasource


    fun getSpecificStock(stock: List<Stock>, marketHashName: String, quality: Quality, isCraftable: Boolean, effect: String = "0"): Stock? {
        return stock.find {
            it.marketHashName == marketHashName
                    && it.quality == quality
                    && it.craftability == isCraftable
                    && it.unusual == effect
        }
    }

    fun getSpecificBotStock(marketHashName: String, quality: Quality, isCraftable: Boolean, effect: String = "0"): Stock? {
        return getSpecificStock(itemDatasource.botStock, marketHashName, quality, isCraftable, effect)
    }

    fun getSpecificUserStock(marketHashName: String, quality: Quality, isCraftable: Boolean, effect: String = "0"): Stock? {
        return getSpecificStock(itemDatasource.userStock, marketHashName, quality, isCraftable, effect)
    }

    fun addItem(item: InventoryItem, type: CartItemType) {
        val current = _cart.value.toMutableList()

        val existing = current.find {
            it.getFirst().classid == item.classid &&
            it.getFirst().instanceid == item.instanceid
        }

        if (existing != null) {
            existing.items
        } else {
            current.add(CartItem(listOf(item).toMutableList(), "", type))
        }

        _cart.value = current
    }

    fun removeItem(item: InventoryItem) {
        val current = _cart.value.toMutableList()

        val existing = current.find {
            it.items.contains(item)
        }

        existing?.items?.filter { it != item }

        _cart.value = current
    }

    fun clearItem(cartItem: CartItem) {
        _cart.value = _cart.value.filter { it != cartItem }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun getTotalPrice(): Double {
        return _cart.value.sumOf { it.getPrice(backpackDatasource) }
    }
}
