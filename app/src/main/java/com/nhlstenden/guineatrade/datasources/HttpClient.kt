package com.nhlstenden.guineatrade.datasources

import okhttp3.OkHttpClient

data class HttpClient(
    val client: OkHttpClient = OkHttpClient(),
//    val baseApiUrl: String = "https://secprog.infinite-night.com/api/v1",
    val baseApiUrl: String = "http://192.168.2.50:3000/api/v1",

    val authRegister: String = "$baseApiUrl/auth/register",
    val authLogin: String = "$baseApiUrl/auth/login",
    val authLogout: String = "$baseApiUrl/auth/logout",
    val authLogoutAll: String = "$baseApiUrl/auth/logout/all",
    val authRefresh: String = "$baseApiUrl/auth/refresh",
    val authMe: String = "$baseApiUrl/auth/me",
    val auth2fATotpRegister: String = "$baseApiUrl/auth/2fa/totp/register",
    val auth2fATotpVerify: String = "$baseApiUrl/auth/2fa/totp/verify",
    val auth2fATotpReset: String = "$baseApiUrl/auth/2fa/totp/reset",

    val steamAuth: String = "$baseApiUrl/steam/auth",
    val steamAuthCallback: String = "$baseApiUrl/steam/auth/callback",
    val steamProfile: String = "$baseApiUrl/steam/profile",
    val steamInventory: String = "$baseApiUrl/steam/inventory",
    val steamInventoryAppId: Function1<Int, String> = { appId: Int -> "$$baseApiUrl/steam/inventory/$appId" },

    val backpackPrices: String = "$baseApiUrl/backpack/prices",
    val backpackItemsItemName: Function1<String, String> = { itemName: String -> "$baseApiUrl/backpack/items/$itemName" },
    val backpackCurrencies: String = "$baseApiUrl/backpack/currencies",

    val paymentsCreate: String = "$baseApiUrl/payments/create",
    val paymentsPaymentId: Function1<Int, String> = { paymentId: Int -> "$$baseApiUrl/payments/$paymentId" },
    val paymentsWebhookMollie: String = "$baseApiUrl/payments/webhook/mollie",
    val paymentsHistory: String = "$baseApiUrl/payments/history",

    val trades: String = "$baseApiUrl/trades",
    val tradesTradeId: Function1<Int, String> = { tradeId: Int -> "$$baseApiUrl/trades/$tradeId" },
    val tradesTradeIdAccept: Function1<Int, String> = { tradeId: Int -> "$$baseApiUrl/trades/$tradeId/accept" },
    val tradesTradeIdCancel: Function1<Int, String> = { tradeId: Int -> "$$baseApiUrl/trades/$tradeId/cancel" },
    val tradesTradeIdDecline: Function1<Int, String> = { tradeId: Int -> "$$baseApiUrl/trades/$tradeId/decline" }
)

