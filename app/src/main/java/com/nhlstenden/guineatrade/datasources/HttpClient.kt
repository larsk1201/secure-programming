package com.nhlstenden.guineatrade.datasources

import okhttp3.OkHttpClient

data class HttpClient(
    val client: OkHttpClient = OkHttpClient(),
//    val baseApiUrl: String = "https://secprog.infinite-night.com/api/v1",
    val baseApiUrl: String = "http://10.230.150.134:3000/api/v1",

    val authRegister: String = "$baseApiUrl/auth/register",
    val authLogin: String = "$baseApiUrl/auth/login",
    val authLogout: String = "$baseApiUrl/auth/logout",
    val authLogoutAll: String = "$baseApiUrl/auth/logout/all",
    val authRefresh: String = "$baseApiUrl/auth/refresh",
    val authMe: String = "$baseApiUrl/auth/me",
    val authMfaTotpRegister: String = "$baseApiUrl/auth/mfa/totp/register",
    val authMfaTotpVerify: String = "$baseApiUrl/auth/mfa/totp/verify",
    val authMfaTotpReset: String = "$baseApiUrl/auth/mfa/totp/reset",
    val authSteam: String = "$baseApiUrl/auth/steam",

    val steamInventory: String = "$baseApiUrl/steam/inventory",
    val userInventory: String = "$baseApiUrl/user/inventory",

    val backpackPrices: String = "$baseApiUrl/backpack/prices",

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

