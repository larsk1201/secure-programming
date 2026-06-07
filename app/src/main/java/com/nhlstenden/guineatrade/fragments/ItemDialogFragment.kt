package com.nhlstenden.guineatrade.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.BackpackDatasource
import com.nhlstenden.guineatrade.datasources.Qualty
import javax.inject.Inject

class ItemDialogFragment(val gridViewModel: GridViewModel): DialogFragment() {

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

        weaponText.text = gridViewModel.itemName

        Glide.with(requireContext())
            .load(gridViewModel.imageUrl)
            .placeholder(R.drawable.item_not_found)
            .error(R.drawable.item_not_found)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(weaponIcon)

        view.findViewById<Button>(R.id.item_popup_ok).setOnClickListener {
            this@ItemDialogFragment.dismiss()
        }

        view.findViewById<Button>(R.id.item_popup_cancel).setOnClickListener {
            this@ItemDialogFragment.dismiss()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.item_popup_details)

//        val item = this.backpackDatasource.prices?.items[this.gridViewModel.itemName]

        val items = listOf(Item(
            Qualty.STRANGE,
            "Craftable",
            emptyList()
        ))
        val adapter = ItemAdapter(items)
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
        val effectName: Qualty,
        val craftability: String,
        val effects: List<Effect>,
    )

    data class Effect(
        val effectId: String,
        val price: Int,
    )

    class ItemAdapter(private val items: List<Item>) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item_price, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int {
            return items.size
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemCard = itemView.findViewById<CardView>(R.id.item_card)
            val effectName = itemView.findViewById<TextView>(R.id.item_effect_name)
            val effects = itemView.findViewById<RecyclerView>(R.id.item_effect_list)

            fun bind(item: Item) {
                effectName.text = "${item.effectName.toString()} - ${item.craftability}"
                itemCard.setCardBackgroundColor(item.effectName.toColour(itemView.context))
            }
        }
    }
}