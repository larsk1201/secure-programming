package com.nhlstenden.guineatrade.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.BackpackDatasource
import com.nhlstenden.guineatrade.datasources.SteamBotDatasource
import com.nhlstenden.guineatrade.datasources.UserDatasource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject


data class GridViewModel(
    val itemName: String,
    val imageUrl: String,
)

@AndroidEntryPoint
class StoreFragment : Fragment() {

    @Inject
    lateinit var userDatasource: UserDatasource
    @Inject
    lateinit var backpackDatasource: BackpackDatasource

    @Inject
    lateinit var steamBotDatasource: SteamBotDatasource

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

        val gridAdapter = GridAdapter(requireContext(), ArrayList(), this)
        itemGrid.adapter = gridAdapter

        if (this@StoreFragment.backpackDatasource.prices == null) {
            lifecycleScope.launch {
                val hasBackpack = async {
                    this@StoreFragment.backpackDatasource.getPrices(this@StoreFragment.userDatasource.tokens.jwtSave)
                }.await()
                if (!hasBackpack) {
                    Toast.makeText(context, "Unable to get pricing data", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val hasSteamData = async {
                    this@StoreFragment.steamBotDatasource.getInventoryData(this@StoreFragment.userDatasource.tokens.jwtSave)
                }.await()
                if (!hasSteamData) {
                    Toast.makeText(context, "Unable to get pricing data", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                populatePage(lastUpdate, gridAdapter)
            }
        } else {
            populatePage(lastUpdate, gridAdapter)
        }
    }

    fun populatePage(lastUpdate: TextView, adapter: GridAdapter) {
        if (this@StoreFragment.backpackDatasource.prices == null) {
            lastUpdate.text = getString(R.string.store_no_price_data)
            return
        }
        lastUpdate.text = this@StoreFragment.backpackDatasource.formatInstantToString()

        for (key: String in this@StoreFragment.backpackDatasource.prices!!.items.keys) {
            val gridViewModel = GridViewModel(
                key,
                this@StoreFragment.backpackDatasource.prices!!.items[key]?.icon ?: ""
            )
            adapter.add(gridViewModel)
        }

        adapter.notifyDataSetChanged()
    }

    class GridAdapter(
        context: Context,
        list: ArrayList<GridViewModel>,
        val parentFragment: Fragment
    ) : ArrayAdapter<GridViewModel>(context, 0, list) {

        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            var itemView = view
            if (itemView == null) {
                itemView = LayoutInflater.from(context).inflate(R.layout.card_item, parent, false)
            }

            val model = getItem(position)!!
            val textView = itemView.findViewById<TextView>(R.id.weapon_name)
            val imageView = itemView.findViewById<ImageView>(R.id.weapon_icon)

            itemView?.setOnClickListener {
                Log.d("StoreFragment", model.itemName)
                val dialog = ItemDialogFragment(model)

                dialog.show(this@GridAdapter.parentFragment.parentFragmentManager, null)
            }

            textView.text = model.itemName
            imageView.contentDescription = model.imageUrl

            Glide.with(context)
                .load(model.imageUrl)
                .placeholder(R.drawable.item_not_found)
                .error(R.drawable.item_not_found)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)

            return itemView
        }
    }
}