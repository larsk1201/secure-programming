package com.nhlstenden.guineatrade.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.BackpackDatasource
import com.nhlstenden.guineatrade.datasources.Quality
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.view.isVisible
import com.nhlstenden.guineatrade.datasources.Category
import com.nhlstenden.guineatrade.datasources.Item
import com.nhlstenden.guineatrade.utils.Pricing

@AndroidEntryPoint
class ItemDialogFragment(val item: Item): DialogFragment() {
    @Inject
    lateinit var backpackDatasource: BackpackDatasource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.popup_item_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val weaponText = view.findViewById<TextView>(R.id.item_popup_name)
        val weaponIcon = view.findViewById<ImageView>(R.id.item_popup_icon)

        weaponText.text = item.marketHashName

        Glide.with(requireContext())
            .load(item.icon)
            .placeholder(R.drawable.item_not_found)
            .error(R.drawable.item_not_found)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(weaponIcon)

        view.findViewById<Button>(R.id.close_button).setOnClickListener {
            this@ItemDialogFragment.dismiss()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.item_popup_details)

        val itemPrices = this.backpackDatasource.prices?.items[item.marketHashName]?.prices
        val itemList = ArrayList<Item>()

        val sortedItemPrices = itemPrices?.toSortedMap()?.reversed()
        for ((quality, itemPair) in sortedItemPrices!!) {
            if (!itemPair.craftable.isEmpty()) {
                val effectList = ArrayList<Effect>()
                val sortedCraftableItems = itemPair.craftable.toSortedMap(compareBy { it.toInt() })

                for ((key, value) in sortedCraftableItems) {
                    effectList.add(Effect(
                        this.backpackDatasource.unusuals[key] ?: "Default",
                        key,
                        value,
                    ))
                }
                itemList.add(Item(
                    quality,
                    "Craftable",
                    effectList
                ))
            }
            if (!itemPair.uncraftable.isEmpty()) {
                val effectList = ArrayList<Effect>()
                val sortedUncraftableItems = itemPair.craftable.toSortedMap()

                for ((key, value) in sortedUncraftableItems) {
                    effectList.add(Effect(
                        this.backpackDatasource.unusuals[key] ?: "Default",
                        key,
                        value,
                    ))
                }
                itemList.add(Item(
                    quality,
                    "Non-Craftable",
                    effectList
                ))
            }
        }

        val adapter = ItemAdapter(itemList)
        val adapter = this.backpackDatasource.prices?.items[item.marketHashName]?.let { ItemAdapter(it, backpackDatasource) }
        recyclerView.setLayoutManager(LinearLayoutManager(this@ItemDialogFragment.context));
        recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        val dialog = getDialog()
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }

    data class Item(
        val effectName: Quality,
        val craftability: String,
        val effects: List<Effect>,
    )

    data class Effect(
        val effectName: String,
        val effectId: String,
        val price: Int,
    )

    class ItemAdapter(private val items: List<Item>) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    class ItemAdapter(private val item: Item, private val backpackDatasource: BackpackDatasource) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
        val categories: List<Category> = item.getCategories()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item_price, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
            val category: Category = categories[position]
            holder.bind(item, backpackDatasource,category)
        }

        override fun getItemCount(): Int {
            return item.prices.size
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemCard: CardView = itemView.findViewById(R.id.item_card)
            val effectName: TextView = itemView.findViewById(R.id.item_effect_name)
            val effects: RecyclerView = itemView.findViewById(R.id.item_effect_list)

            fun bind(item: Item) {
                effectName.text = "${item.effectName} - ${item.craftability}"
                itemCard.setCardBackgroundColor(item.effectName.toColour(itemView.context))
            fun bind(item: Item,  backpackDatasource: BackpackDatasource, category: Category) {
                effectName.text = category.toName()
                itemCard.setCardBackgroundColor(category.quality.toColour(itemView.context))

                val adapter = EffectAdapter(item.effects)
                val adapter = EffectAdapter(item,  backpackDatasource, category)
                effects.setLayoutManager(LinearLayoutManager(itemView.context));
                effects.adapter = adapter
            }

            class EffectAdapter(private val effects: List<Effect>) : RecyclerView.Adapter<EffectAdapter.ViewHolder>() {
            class EffectAdapter(private val item: Item, private val backpackDatasource: BackpackDatasource,  private val category: Category) : RecyclerView.Adapter<EffectAdapter.ViewHolder>() {
                val effects = item.getSpecificPricingData(category)?.entries?.toList() ?: emptyList()

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item_effect, parent, false)
                    return ViewHolder(item, view)
                }

                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    holder.bind(effects[position])
                    val effectData = effects[position]
                    // cartDatasource,
                }

                override fun getItemCount(): Int {
                    return effects.size
                }

                class ViewHolder(val item: Item, val effectView: View) : RecyclerView.ViewHolder(effectView) {
                    val effectPrice: TextView = effectView.findViewById(R.id.effect_price)
                    val effectName: TextView = effectView.findViewById(R.id.effect_name)
                    val buyPrice: TextView = effectView.findViewById(R.id.buy_price)
                    val sellPrice: TextView = effectView.findViewById(R.id.sell_price)
                    val buyButton: Button = effectView.findViewById(R.id.buy_button)
                    val sellButton: Button = effectView.findViewById(R.id.sell_button)

                    val toggleableGroup = listOf<LinearLayout>(
                        effectView.findViewById(R.id.buy_row),
                        effectView.findViewById(R.id.sell_row),
                        effectView.findViewById(R.id.button_row)
                    )

                    fun bind(effect: Effect) {
                        val realPrice = effect.price.toDouble() / 100.0
                    fun bind(item: Item, backpackDatasource: BackpackDatasource,  category: Category, effectId: String, price: Int) {
                        val realPrice = price.toDouble() / 100.0
                        val effectDisplayName = backpackDatasource.getUnusualName(effectId)

                        effectView.setOnClickListener {
                            if (toggleableGroup.first().isVisible) {
                                toggleableGroup.forEach { it.visibility = View.GONE }
                            } else {
                                toggleableGroup.forEach { it.visibility = View.VISIBLE }
                            }
                        }

                        buyButton.setOnClickListener {
//                            TODO: Add item to cart
                            Log.d("ItemDialogFragment", "Sold item with effect ${effect.effectName} for $realPrice")
                            Log.d("ItemDialogFragment", "Sold item with effect $effectDisplayName for $realPrice")
                        }
                        sellButton.setOnClickListener {
//                            TODO: Add item to cart
                            Log.d("ItemDialogFragment", "Sold item with effect ${effect.effectName} for $realPrice")
                            Log.d("ItemDialogFragment", "Sold item with effect $effectDisplayName for $realPrice")
                        }

                        buyPrice.text = Pricing.toFormattedPriceString(realPrice * Pricing.BUY_MODIFIER)
                        sellPrice.text = Pricing.toFormattedPriceString(realPrice * Pricing.SELL_MODIFIER)
                        effectName.text = effectDisplayName
                        effectPrice.text = Pricing.toFormattedPriceString(realPrice)
                    }
                }
            }
        }
    }
}