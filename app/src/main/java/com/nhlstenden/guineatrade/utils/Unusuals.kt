package com.nhlstenden.guineatrade.utils

import android.content.Context
import com.nhlstenden.guineatrade.R
import kotlinx.serialization.json.Json

class Unusuals {
    companion object {
        private lateinit var unusuals: Map<String, String>

        fun init(context: Context) {
            unusuals = Json.decodeFromString(
                context.resources.openRawResource(R.raw.unusuals).bufferedReader().use { it.readText() }
            )
        }

        fun getUnusualName(id: String): String = unusuals[id] ?: "Default"
    }
}