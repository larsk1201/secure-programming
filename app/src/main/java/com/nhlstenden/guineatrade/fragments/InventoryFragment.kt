package com.nhlstenden.guineatrade.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.BackpackDatasource
import com.nhlstenden.guineatrade.datasources.ItemDatasource
import com.nhlstenden.guineatrade.datasources.UserDatasource
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class ItemCounter(
    val name: String,
    val iconUrl: String,
    val quantity: Int,
)

@AndroidEntryPoint
class InventoryFragment : Fragment() {

    @Inject
    lateinit var itemDatasource: ItemDatasource

    @Inject
    lateinit var userDatasource: UserDatasource

    @Inject
    lateinit var backpackDatasource: BackpackDatasource

    private var inventoryItemCounters = listOf<ItemCounter>()

    private fun loadInventory(
        profileName: TextView,
        itemGridView: GridView,
    ) {
        profileName.text = "My Inventory"

        lifecycleScope.launch {

            if (this@InventoryFragment.itemDatasource.isEmpty()) {
                val result = async {
                    this@InventoryFragment.itemDatasource.setupInventories()
                }.await()
                if (!result) {
                    Toast.makeText(context, "Unable to get inventory data", Toast.LENGTH_SHORT).show()
                    return@launch
                }
            }

            val items = this@InventoryFragment.itemDatasource.userInventory
            val descriptionMap = items.associateBy {
                "${it.classid}_${it.instanceid}"
            }

            val groupedAssets = items.groupBy {
                "${it.classid}_${it.instanceid}"
            }

            inventoryItemCounters = groupedAssets.mapNotNull { (key, assets) ->

                val description = descriptionMap[key]

                description?.let {
                    ItemCounter(
                        name = it.marketHashName,
                        iconUrl = backpackDatasource.prices!!.items[it.marketHashName]!!.icon,
                        quantity = assets.size
                    )
                }
            }

            updateGrid("", itemGridView)
        }
    }

    fun updateGrid(filter: String, itemGrid: GridView) {
        try {
            val gridAdapter = ItemGridAdapter(requireContext(), searchItems(filter))
            itemGrid.adapter = gridAdapter
            gridAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Log.d("InventoryFragment", e.message.toString())
            Toast.makeText(context, "Not a valid search query", Toast.LENGTH_SHORT).show()
        }
    }

    fun searchItems(filter: String): ArrayList<ItemCounter> {
        val pattern = Regex(filter, RegexOption.IGNORE_CASE)

        val list = ArrayList<ItemCounter>()
        for (itemCounter: ItemCounter in this@InventoryFragment.inventoryItemCounters) {
            if (pattern.containsMatchIn(itemCounter.name)) {
                list.add(itemCounter)
            }
        }

        return list
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

        val itemGridView =
            view.findViewById<GridView>(R.id.itemGridView)

        val searchButton =
            view.findViewById<ImageView>(R.id.search_button)

        val searchPattern =
            view.findViewById<EditText>(R.id.search_pattern)

        searchButton.setOnClickListener {
            val pattern = searchPattern.text.toString()
            updateGrid(pattern, itemGridView)
        }

        searchPattern.addTextChangedListener {
            val searchText = searchPattern.text.toString()
            updateGrid(searchText, itemGridView)
        }

        val steamId = userDatasource.steamId

        if (steamId != 0L) {
            loadInventory(
                profileName,
                itemGridView
            )
        } else {
            profileName.text = getString(R.string.no_steam_account_linked)
        }
        return view
    }
}