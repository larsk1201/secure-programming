package com.nhlstenden.guineatrade.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
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

        val profileName =
            view.findViewById<TextView>(R.id.profileName)

        val itemsContainer =
            view.findViewById<LinearLayout>(R.id.itemsContainer)

        searchButton.setOnClickListener {
            val steamId = steamIdInput.text.toString()

            lifecycleScope.launch {
                try
                {
                    val response = api.getInventory(steamId)
                    val inventoryText = "Inventory of $steamId"

                    profileName.text = inventoryText
                    itemsContainer.removeAllViews()

                    val descriptionMap = response.descriptions.associateBy{
                        "${it.classid}_${it.instanceid}"
                    }

                    response.assets.forEach { asset ->
                        val key = "${asset.classid}_${asset.instanceid}"
                        val description = descriptionMap[key]

                        if (description != null)
                        {
                            val itemText = TextView(requireContext())

                            itemText.text = description.name
                            itemText.textSize = 16f
                            itemText.setPadding(0, 8, 0, 8)

                            itemsContainer.addView(itemText)
                        }
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