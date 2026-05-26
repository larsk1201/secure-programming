package com.nhlstenden.guineatrade.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.api.SteamApi
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

        val profileName =
            view.findViewById<TextView>(R.id.profileName)

        val itemsRecyclerView =
            view.findViewById<RecyclerView>(R.id.itemsRecyclerView)

        searchButton.setOnClickListener {
            val steamId = steamIdInput.text.toString()

            lifecycleScope.launch {
                try
                {
                    val response = api.getInventory(steamId)
                    val inventoryText = "Inventory of $steamId"

                    profileName.text = inventoryText

                    val descriptionMap = response.descriptions.associateBy {
                        "${it.classid}_${it.instanceid}"
                    }

                    val inventoryItems = mutableListOf<InventoryItem>()

                    response.assets.forEach { asset ->

                        val key = "${asset.classid}_${asset.instanceid}"

                        val description = descriptionMap[key]

                        if (description != null)
                        {
                            inventoryItems.add(
                                InventoryItem(
                                    name = description.name,
                                    iconUrl = description.iconUrl
                                )
                            )
                        }
                    }
                    val adapter = InventoryAdapter(inventoryItems)

                    itemsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                    itemsRecyclerView.adapter = adapter
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