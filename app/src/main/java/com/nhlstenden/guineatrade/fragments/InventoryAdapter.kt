package com.nhlstenden.guineatrade.fragments

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
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

    @SuppressLint("SetTextI18n")
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

            val context = holder.itemView.context

            val layout = LinearLayout(context)

            layout.orientation = LinearLayout.VERTICAL

            layout.setPadding(50, 40, 50, 10)

            val imageView = ImageView(context)

            val imageParams = LinearLayout.LayoutParams(
                300,
                300
            )

            imageParams.bottomMargin = 30

            imageView.layoutParams = imageParams

            val imageUrl =
                "https://community.akamai.steamstatic.com/economy/image/${item.iconUrl}"

            imageView.load(imageUrl) {

                placeholder(R.drawable.app_icon)

                error(R.drawable.app_icon)

                fallback(R.drawable.app_icon)
            }

            val textView = TextView(context)

            textView.text = buildString {
                appendLine("Amount owned: ${item.quantity}")
                appendLine("Value: will be added soon")
                appendLine("Rarity: will be added soon")
                appendLine("Estimated price: will be added soon")
            }

            textView.textSize = 16f

            layout.addView(imageView)

            layout.addView(textView)

            val dialog = android.app.AlertDialog.Builder(context)

            dialog.setTitle(item.name)

            dialog.setView(layout)

            dialog.setPositiveButton("Close") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }

            dialog.show()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}