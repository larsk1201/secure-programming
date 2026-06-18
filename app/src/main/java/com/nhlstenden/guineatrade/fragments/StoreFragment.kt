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
import com.nhlstenden.guineatrade.datasources.Item
import com.nhlstenden.guineatrade.datasources.ItemDatasource
import com.nhlstenden.guineatrade.datasources.UserDatasource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StoreFragment : Fragment() {

    @Inject
    lateinit var userDatasource: UserDatasource
    @Inject
    lateinit var backpackDatasource: BackpackDatasource

    @Inject
    lateinit var itemDatasource: ItemDatasource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_store, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemGrid = view.findViewById<GridView>(R.id.item_grid)
        val lastUpdate = view.findViewById<TextView>(R.id.last_update)
        val searchButton = view.findViewById<ImageView>(R.id.search_button)
        val searchPattern = view.findViewById<EditText>(R.id.search_pattern)

        if (this@StoreFragment.backpackDatasource.prices == null) {
            lifecycleScope.launch {
                val hasBackpack = async {
                    this@StoreFragment.backpackDatasource.getPrices(this@StoreFragment.userDatasource.tokens.jwtSave)
                }.await()
                if (!hasBackpack) {
                    Toast.makeText(context, "Unable to get pricing data", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                if (this@StoreFragment.itemDatasource.isInventoryEmpty()) {
                    val hasSteamData = async {
                        this@StoreFragment.itemDatasource.setupInventories()
                    }.await()
                    if (!hasSteamData) {
                        Toast.makeText(context, "Unable to get pricing data", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                }
                populatePage(lastUpdate, itemGrid)
            }
        } else {
            populatePage(lastUpdate, itemGrid)
        }

        searchButton.setOnClickListener {
            val pattern = searchPattern.text.toString()
            updateGrid(pattern, itemGrid)
        }
        searchPattern.addTextChangedListener {
            val searchText = searchPattern.text.toString()
            updateGrid(searchText, itemGrid)
        }
    }

    fun populatePage(lastUpdate: TextView, itemGrid: GridView) {
        if (this@StoreFragment.backpackDatasource.prices == null) {
            lastUpdate.text = getString(R.string.store_no_price_data)
            return
        }

        updateGrid("", itemGrid)

        lastUpdate.text = this@StoreFragment.backpackDatasource.formatInstantToString(requireContext())
    }

    fun updateGrid(filter: String, itemGrid: GridView) {
        try {
            val gridAdapter = StoreGridAdapter(requireContext(), searchItems(filter), this)
            itemGrid.adapter = gridAdapter
            gridAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Log.d("StoreFragment", e.message.toString())
            Toast.makeText(context, "Not a valid search query", Toast.LENGTH_SHORT).show()
        }
    }

    fun searchItems(filter: String): ArrayList<Item> {
        val pattern = Regex(filter, RegexOption.IGNORE_CASE)

        val list = ArrayList<Item>()
        for (item: Item in this@StoreFragment.backpackDatasource.prices!!.items.values) {
            if (pattern.containsMatchIn(item.marketHashName)) {
                list.add(item)
            }
        }

        return list
    }
}