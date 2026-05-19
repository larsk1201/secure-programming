package com.nhlstenden.guineatrade.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nhlstenden.guineatrade.R
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )
        val steamIdInput =
            view.findViewById<EditText>(R.id.steamIdInput)

        val searchButton =
            view.findViewById<Button>(R.id.searchSteamId)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://steamcommunity.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(SteamApi::class.java)

        searchButton.setOnClickListener {
            val steamId = steamIdInput.text.toString()

            lifecycleScope.launch {
                try
                {
                    val response = api.getInventory(steamId)
                    response.descriptions.forEach {
                        Log.d("STEAM_ITEM", it.name)
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
        }
        return view
    }
}