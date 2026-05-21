package com.nhlstenden.guineatrade.fragments

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

data class InventoryResponse(
    val success: Int,
    val descriptions: List<Description>,
    val assets: List<Asset>
)

data class Description(
    val name: String,
    @SerializedName("icon_url")
    val iconUrl: String,
    val classid: String,
    val instanceid: String
)

data class Asset(
    val assetid: String,
    val classid: String,
    val instanceid: String
)

interface SteamApi {

    @GET("inventory/{steamId}/440/2") // Test ID: 76561198995576702
    suspend fun getInventory(
        @Path("steamId") steamId: String
    ): InventoryResponse
}