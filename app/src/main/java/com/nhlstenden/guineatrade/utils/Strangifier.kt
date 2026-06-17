package com.nhlstenden.guineatrade.utils

import android.content.Context
import com.nhlstenden.guineatrade.R
import kotlinx.serialization.json.Json

class Strangifier {
    companion object {
        private lateinit var strangifiers: Map<String, String>

        fun init(context: Context) {
            strangifiers = Json.decodeFromString(
                context.resources.openRawResource(R.raw.strangifier).bufferedReader().use { it.readText() }
            )
        }

        fun getStrangifierName(id: String): String = strangifiers[id] ?: "Default"
    }
}