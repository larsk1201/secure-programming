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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.view.isVisible
import com.nhlstenden.guineatrade.datasources.Category
import com.nhlstenden.guineatrade.datasources.Item
import com.nhlstenden.guineatrade.utils.Pricing
import com.nhlstenden.guineatrade.utils.Strangifier
import com.nhlstenden.guineatrade.utils.Unusuals

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

        val adapter = this.backpackDatasource.prices?.items[item.marketHashName]?.let { ItemAdapter(it) }
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

    class ItemAdapter(private val item: Item) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
        val categories: List<Category> = item.getCategories()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item_price, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val category: Category = categories[position]
            holder.bind(item, category)
        }

        override fun getItemCount(): Int {
            return item.prices.size
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemCard: CardView = itemView.findViewById(R.id.item_card)
            val effectName: TextView = itemView.findViewById(R.id.item_effect_name)
            val effects: RecyclerView = itemView.findViewById(R.id.item_effect_list)

            fun bind(item: Item, category: Category) {
                effectName.text = category.toName()
                itemCard.setCardBackgroundColor(category.quality.toColour(itemView.context))

                val adapter = EffectAdapter(item, category)
                effects.setLayoutManager(LinearLayoutManager(itemView.context));
                effects.adapter = adapter
            }

            class EffectAdapter(private val item: Item,  private val category: Category) : RecyclerView.Adapter<EffectAdapter.ViewHolder>() {
                val effects = item.getSpecificPricingData(category)?.entries?.toList() ?: emptyList()

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item_effect, parent, false)
                    return ViewHolder(item, view)
                }

                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    val effectData = effects[position]
                    holder.bind(item, category, effectData.key, effectData.value)
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

                    fun bind(item: Item, category: Category, effectId: String, price: Int) {
                        val realPrice = price.toDouble() / 100.0
                        var effectDisplayName = if (item.marketHashName != ("Strangifier")) Unusuals.getUnusualName(effectId) else Strangifier.getStrangifierName(effectId)

                        effectView.setOnClickListener {
                            if (toggleableGroup.first().isVisible) {
                                toggleableGroup.forEach { it.visibility = View.GONE }
                            } else {
                                toggleableGroup.forEach { it.visibility = View.VISIBLE }
                            }
                        }

                        buyButton.setOnClickListener {
                            Log.d("ItemDialogFragment", "Sold item with effect $effectDisplayName for $realPrice")
                        }
                        sellButton.setOnClickListener {
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