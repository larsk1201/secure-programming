package com.nhlstenden.guineatrade.datasources

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

data class CartItem(
    val itemName: String,
    val quality: Quality,
    val effectId: String,
    val craftable: String,

    val defindex: List<Int>,
    val assetId: String,

    val imageUrl: String,
    var quantity: Int = 1,
    var type: CartItemType = CartItemType.BUY
) {
    fun getPrice(backpackDatasource: BackpackDatasource): Double {
        return backpackDatasource.prices?.getSpecificPricing(itemName, quality, craftable, effectId)
            ?.div(100)?.times(quantity)?.times((if (type == CartItemType.SELL) Pricing.SELL_MODIFIER else Pricing.BUY_MODIFIER)) ?: 0.0
    }
}

@Singleton
class CartDatasource @Inject constructor(){
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items

    @Inject
    lateinit var backpackDatasource: BackpackDatasource

    fun addItem(item: CartItem) {
        val current = _items.value.toMutableList()

        val existing = current.find {
            it.defindex == item.defindex
        }

        if (existing != null) {
            existing.quantity++
        } else {
            current.add(item)
        }

        _items.value = current
    }

    fun addItem(item: Item, category: Category, effectId: String, type: CartItemType) {
        // TODO: Implement AssetID
        addItem(CartItem(item.marketHashName, category.quality, effectId, category.craftable, item.defindex,  "", item.icon, type = type))
    }

    fun removeItem(productId: Int) {
        val current = _items.value.toMutableList()

        val existing = current.find {
            it.defindex.contains(productId)
        }

        if (existing != null) {
            existing.quantity--
        }

        _items.value = current
    }

    fun clearItem(productId: Int) {
        _items.value = _items.value.filter { !it.defindex.contains(productId) }
    }

    fun clearCart() {
        _items.value = emptyList()
    }

    fun getTotalPrice(): Double {
        return _items.value.sumOf { it.getPrice(backpackDatasource) }
    }
}
