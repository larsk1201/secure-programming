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
import com.nhlstenden.guineatrade.datasources.UserDatasource
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InventoryFragment : Fragment() {

    @Inject
    lateinit var inventoryDatasource: InventoryDatasource

    @Inject
    lateinit var userDatasource: UserDatasource

    private fun loadInventory(
        steamId: String,
        profileName: TextView,
        itemsRecyclerView: RecyclerView,
    ) {
        lifecycleScope.launch {

            val response = inventoryDatasource.getInventory(steamId)

            if (response == null) {
                profileName.text = "Failed to load inventory"
                return@launch
            }

            profileName.text = "My Inventory"

            val descriptionMap = response.descriptions.associateBy {
                "${it.classid}_${it.instanceid}"
            }

            val groupedAssets = response.assets.groupBy {
                "${it.classid}_${it.instanceid}"
            }

            val inventoryItems = groupedAssets.mapNotNull { (key, assets) ->

                val description = descriptionMap[key]

                description?.let {
                    InventoryItem(
                        name = it.name,
                        iconUrl = it.icon_url,
                        quantity = assets.size
                    )
                }
            }
            itemsRecyclerView.layoutManager =
                GridLayoutManager(requireContext(), 3)

            itemsRecyclerView.adapter =
                InventoryAdapter(inventoryItems)
        }
    }

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
        val profileName =
            view.findViewById<TextView>(R.id.profileName)

        val itemsRecyclerView =
            view.findViewById<RecyclerView>(R.id.itemsRecyclerView)

        val steamId = userDatasource.steamId

        if (steamId != 0L) {
            loadInventory(
                steamId.toString(),
                profileName,
                itemsRecyclerView
            )
        } else {
            profileName.text = "No Steam account linked"
        }
        return view
    }
}