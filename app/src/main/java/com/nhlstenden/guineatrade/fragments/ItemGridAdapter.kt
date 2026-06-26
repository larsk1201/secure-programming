package com.nhlstenden.guineatrade.fragments

import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nhlstenden.guineatrade.R

class ItemGridAdapter(
    private val context: Context,
    private val list: ArrayList<ItemCounter>
) : ArrayAdapter<ItemCounter>(context, 0, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.card_item, parent, false)
        }

        val itemImage: ImageView = itemView.findViewById(R.id.weapon_icon)
        val itemName: TextView = itemView.findViewById(R.id.weapon_name)

        val model = getItem(position)!!
        itemName.text = model.name

        Glide.with(context)
            .load(model.iconUrl)
            .placeholder(R.drawable.item_not_found)
            .error(R.drawable.item_not_found)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(itemImage)

        itemView.setOnClickListener {
            val activity = itemView.context
                .let { ctx ->
                    var c = ctx
                    while (c is ContextWrapper) {
                        if (c is FragmentActivity) return@let c
                        c = c.baseContext
                    }
                    throw IllegalStateException("No FragmentActivity found")
                }

            InventoryItemDialogFragment(model)
                .show(activity.supportFragmentManager, null)
        }

        return itemView
    }
}