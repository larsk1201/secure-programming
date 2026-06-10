package com.nhlstenden.guineatrade.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.nhlstenden.guineatrade.R

class HomeFragment : Fragment() {

    data class HomeSteamItem(
        val name: String,
        val imageResId: Int
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val todayGrid: GridLayout = view.findViewById(R.id.today_most_valuable_grid)
        val userGrid: GridLayout = view.findViewById(R.id.user_most_valuable_grid)

        val todayItems = listOf(
            HomeSteamItem("Golden Frying Pan", R.drawable.shopping_cart_24px),
            HomeSteamItem("Australium Rocket Launcher", R.drawable.shopping_cart_24px),
            HomeSteamItem("Australium Scattergun", R.drawable.shopping_cart_24px),
            HomeSteamItem("Burning Team Captain", R.drawable.shopping_cart_24px),
            HomeSteamItem("Golden Wrench", R.drawable.shopping_cart_24px),
            HomeSteamItem("Max's Head", R.drawable.shopping_cart_24px),
        ).take(6)

        val userItems = listOf(
            HomeSteamItem("Strange Rocket Launcher", R.drawable.shopping_cart_24px),
            HomeSteamItem("Unusual Hat", R.drawable.shopping_cart_24px),
            HomeSteamItem("Mann Co. Key", R.drawable.shopping_cart_24px),
            HomeSteamItem("Festive Scattergun", R.drawable.shopping_cart_24px),
            HomeSteamItem("Tour of Duty Ticket", R.drawable.shopping_cart_24px),
            HomeSteamItem("Killstreak Kit", R.drawable.shopping_cart_24px),
        ).take(6)

        addItemsToGrid(todayGrid, todayItems)
        addItemsToGrid(userGrid, userItems)

        return view
    }

    private fun addItemsToGrid(
        grid: GridLayout,
        items: List<HomeSteamItem>
    ) {
        grid.removeAllViews()

        items.take(6).forEach { item ->
            val itemView = layoutInflater.inflate(
                R.layout.card_item,
                grid,
                false
            )

            val icon: ImageView = itemView.findViewById(R.id.weapon_icon)
            val name: TextView = itemView.findViewById(R.id.weapon_name)
            val cardHeight = (150 * resources.displayMetrics.density).toInt()

            icon.setImageResource(item.imageResId)
            name.text = item.name

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = cardHeight
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(6, 6, 6, 6)
            }

            itemView.layoutParams = params
            grid.addView(itemView)
        }
    }
}