package com.nhlstenden.guineatrade.utils

import com.nhlstenden.guineatrade.datasources.CartItemType
import com.nhlstenden.guineatrade.datasources.Price

class Pricing {
    companion object {
        const val BUY_MODIFIER: Double = 1.1
        const val SELL_MODIFIER: Double = 0.9

        fun toFormattedPriceString(price: Double): String {
            return "$%.2f".format(price)
        }

        fun isTradeAllowed(price: Price): Boolean {
            if (price.type == CartItemType.BUY && price.price < 0.5) return false
            return true
        }
    }
}