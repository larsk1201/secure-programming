package com.nhlstenden.guineatrade.fragments

import retrofit2.http.GET
import retrofit2.http.Path

data class InventoryResponse(
    val success: Int,
    val descriptions: List<Description>
)

data class Description(
    val name: String,
    val icon_url: String
)

interface SteamApi {

    @GET("inventory/{steamId}/440/2")
    suspend fun getInventory(
        @Path("steamId") steamId: String
    ): InventoryResponse
}