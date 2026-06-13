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
import com.nhlstenden.guineatrade.datasources.InventoryDatasource
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InventoryFragment : Fragment() {

    @Inject
    lateinit var inventoryDatasource: InventoryDatasource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_inventory,
            container,
            false
        )
        val steamIdInput =
            view.findViewById<EditText>(R.id.steamIdInput)

        val searchButton =
            view.findViewById<Button>(R.id.searchSteamId)

        val profileName =
            view.findViewById<TextView>(R.id.profileName)

        val itemsRecyclerView =
            view.findViewById<RecyclerView>(R.id.itemsRecyclerView)

        searchButton.setOnClickListener {

            val steamId = steamIdInput.text.toString()

            lifecycleScope.launch {

                val response = inventoryDatasource.getInventory(steamId)

                if (response == null) {
                    profileName.text = "Failed to load inventory"
                    return@launch
                }

                profileName.text = "Inventory of $steamId"

                val descriptionMap = response.descriptions.associateBy {
                    "${it.classid}_${it.instanceid}"
                }

                val inventoryItems = response.assets.mapNotNull { asset ->

                    val key = "${asset.classid}_${asset.instanceid}"
                    val description = descriptionMap[key]

                    description?.let {
                        InventoryItem(
                            name = it.name,
                            iconUrl = it.icon_url
                        )
                    }
                }

                itemsRecyclerView.layoutManager =
                    GridLayoutManager(requireContext(), 2)

                itemsRecyclerView.adapter =
                    InventoryAdapter(inventoryItems)
            }
        }
        return view
    }
}