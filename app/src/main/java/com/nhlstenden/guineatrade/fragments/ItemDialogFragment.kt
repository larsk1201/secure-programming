package com.nhlstenden.guineatrade.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.BackpackDatasource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.view.isVisible
import com.nhlstenden.guineatrade.datasources.ButtonColor
import com.nhlstenden.guineatrade.datasources.CartDatasource
import com.nhlstenden.guineatrade.datasources.Category
import com.nhlstenden.guineatrade.datasources.Item
import com.nhlstenden.guineatrade.utils.Pricing
import androidx.lifecycle.lifecycleScope
import com.nhlstenden.guineatrade.datasources.CartItemType
import com.nhlstenden.guineatrade.datasources.ItemDatasource
import com.nhlstenden.guineatrade.datasources.Quality
import com.nhlstenden.guineatrade.datasources.SpecificItem
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ItemDialogFragment(val item: Item): DialogFragment() {
    @Inject
    lateinit var backpackDatasource: BackpackDatasource
    @Inject
    lateinit var cartDatasource: CartDatasource
    @Inject
    lateinit var itemDatasource: ItemDatasource

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

        weaponText.text = when(item.marketHashName) {
            "Refined Metal", "Reclaimed Metal", "Scrap Metal" -> item.marketHashName + " (x20)"
            else -> item.marketHashName
        } as CharSequence?

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
        recyclerView.setLayoutManager(LinearLayoutManager(this@ItemDialogFragment.context))

        val adapter = ItemAdapter(item, cartDatasource)
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            if (itemDatasource.isStockEmpty()) {
                val success = async { itemDatasource.setupStock() }.await()
                if (!success) {
                    Toast.makeText(context, "Unable to get item stocking", Toast.LENGTH_SHORT).show()
                }
            }

            if (itemDatasource.botStock.isNotEmpty() || itemDatasource.userStock.isNotEmpty()) {
                adapter.notifyDataSetChanged()
            }
        }
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

    class ItemAdapter(private val item: Item, private val cartDatasource: CartDatasource) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
        val categories: List<Category> = item.getCategories()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item_price, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val category: Category = categories[position]
            holder.bind(item, category, cartDatasource)
        }

        override fun getItemCount(): Int {
            return this.categories.size
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemCard: CardView = itemView.findViewById(R.id.item_card)
            val effectName: TextView = itemView.findViewById(R.id.item_effect_name)
            val effects: RecyclerView = itemView.findViewById(R.id.item_effect_list)

            fun bind(item: Item, category: Category, cartDatasource: CartDatasource) {
                effectName.text = category.toName()
                itemCard.setCardBackgroundColor(category.quality.toColour(itemView.context))

                val adapter = EffectAdapter(item, category, cartDatasource)
                effects.setLayoutManager(LinearLayoutManager(itemView.context))
                effects.adapter = adapter
            }

            class EffectAdapter(private val item: Item,  private val category: Category, private val cartDatasource: CartDatasource) : RecyclerView.Adapter<EffectAdapter.ViewHolder>() {
                val effects = item.getSpecificPricingData(category)?.entries?.toList() ?: emptyList()

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item_effect, parent, false)
                    return ViewHolder(item, view)
                }

                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    val effectData = effects[position]
                    holder.bind(item, category, effectData.key, effectData.value, cartDatasource)
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

                    fun bind(item: Item, category: Category, unusual: String, price: Int, cartDatasource: CartDatasource) {
                        val realPrice = price.toDouble() / 100.0
                        var effectDisplayName = "Default"
                        if (category.quality == Quality.UNUSUAL) {
                            effectDisplayName = unusual
                        }
                        effectView.setOnClickListener {
                            if (toggleableGroup.first().isVisible) {
                                toggleableGroup.forEach { it.visibility = View.GONE }
                            } else {
                                toggleableGroup.forEach { it.visibility = View.VISIBLE }
                            }
                        }

                        val botStock = cartDatasource.getSpecificBotStock(item.marketHashName, category.quality, category.craftable == "craftable", unusual)
                        val userStock = cartDatasource.getSpecificUserStock(item.marketHashName, category.quality, category.craftable == "craftable", unusual)

                        if (botStock != null && botStock.quantity > 0) {
                            buyButton.isEnabled = true
                            buyButton.setOnClickListener {
                                cartDatasource.addItem(SpecificItem(item.marketHashName,category.craftable == "craftable", category.quality, unusual,), CartItemType.BUY, item.icon)
                            }
                        } else {
                            buyButton.isEnabled = false
                            buyButton.setBackgroundColor(ButtonColor.OUT_OF_STOCK.toColour(itemView.context))
                        }

                        if (userStock != null && userStock.quantity > 0) {
                            sellButton.isEnabled = true
                            sellButton.setOnClickListener {
                                cartDatasource.addItem(SpecificItem(item.marketHashName,category.craftable == "craftable", category.quality, unusual,), CartItemType.SELL, item.icon)
                            }
                        } else {
                            sellButton.isEnabled = false
                            sellButton.setBackgroundColor(ButtonColor.OUT_OF_STOCK.toColour(itemView.context))
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