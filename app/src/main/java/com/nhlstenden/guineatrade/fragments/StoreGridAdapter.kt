package com.nhlstenden.guineatrade.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.Item

class StoreGridAdapter(
    context: Context,
    list: ArrayList<Item>,
    val parentFragment: Fragment
) : ArrayAdapter<Item>(context, 0, list) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var itemView = view
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.card_item, parent, false)
        }

        val model = getItem(position)!!
        val textView = itemView.findViewById<TextView>(R.id.weapon_name)
        val imageView = itemView.findViewById<ImageView>(R.id.weapon_icon)

        itemView?.setOnClickListener {
            val dialog = ItemDialogFragment(model)

            dialog.show(this@StoreGridAdapter.parentFragment.parentFragmentManager, null)
        }

        textView.text = model.marketHashName
        imageView.contentDescription = model.icon

        Glide.with(context)
            .load(model.icon)
            .placeholder(R.drawable.item_not_found)
            .error(R.drawable.item_not_found)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)

        return itemView
    }
}