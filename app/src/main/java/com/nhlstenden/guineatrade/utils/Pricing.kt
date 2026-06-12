package com.nhlstenden.guineatrade.utils

class Pricing {
    companion object {
        const val BUY_MODIFIER: Double = 1.1
        const val SELL_MODIFIER: Double = 0.9

        fun toFormattedPriceString(price: Double): String {
            return "$%.2f".format(price)
        }
    }
}