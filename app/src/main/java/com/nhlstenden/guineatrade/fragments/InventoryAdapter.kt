package com.nhlstenden.guineatrade.fragments

import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nhlstenden.guineatrade.R

class InventoryAdapter(
    private val items: List<InventoryItem>
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    class InventoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.itemImage)
        val itemName: TextView = view.findViewById(R.id.itemName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)

        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {

        val item = items[position]

        holder.itemName.text = item.name

        val imageUrl =
            "https://community.akamai.steamstatic.com/economy/image/${item.iconUrl}"

        holder.itemImage.load(imageUrl) {
            placeholder(R.drawable.app_icon)
            error(R.drawable.app_icon)
            fallback(R.drawable.app_icon)
        }

        holder.itemView.setOnClickListener {

            val activity = holder.itemView.context
                .let { ctx ->
                    var c = ctx
                    while (c is ContextWrapper) {
                        if (c is FragmentActivity) return@let c
                        c = c.baseContext
                    }
                    throw IllegalStateException("No FragmentActivity found")
                }

            InventoryItemDialogFragment(item)
                .show(activity.supportFragmentManager, null)
        }
    }

    override fun getItemCount(): Int = items.size
}