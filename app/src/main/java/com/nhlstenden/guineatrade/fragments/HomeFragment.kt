package com.nhlstenden.guineatrade.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.activities.ProfileActivity
import com.nhlstenden.guineatrade.datasources.BackpackDatasource
import com.nhlstenden.guineatrade.datasources.Item
import com.nhlstenden.guineatrade.datasources.Quality
import com.nhlstenden.guineatrade.datasources.UserDatasource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var backpackDatasource: BackpackDatasource

    @Inject
    lateinit var userDatasource: UserDatasource

    data class HomeSteamItem(
        val item: Item,
        val value: Int
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSteamWarning(view)
        setupMfaWarning(view)

        val unusualGrid: GridLayout = view.findViewById(R.id.unusual_items_grid)
        val collectorsGrid: GridLayout = view.findViewById(R.id.collectors_items_grid)
        val strangeGrid: GridLayout = view.findViewById(R.id.strange_items_grid)
        val uniqueGrid: GridLayout = view.findViewById(R.id.unique_items_grid)
        val metalGrid: GridLayout = view.findViewById(R.id.metal_grid)

        lifecycleScope.launch {
            if (backpackDatasource.prices == null) {
                val success = async {
                    backpackDatasource.getPrices(userDatasource.tokens.jwtSave)
                }.await()

                if (!success) {
                    Toast.makeText(context, "Unable to get pricing data", Toast.LENGTH_SHORT).show()
                    return@launch
                }
            }

            addItemsToGrid(unusualGrid, getMostValuableByQuality(Quality.UNUSUAL))
            addItemsToGrid(collectorsGrid, getMostValuableByQuality(Quality.COLLECTORS))
            addItemsToGrid(strangeGrid, getMostValuableByQuality(Quality.STRANGE))
            addItemsToGrid(uniqueGrid, getMostValuableByQuality(Quality.UNIQUE))

            addItemsToGrid(
                metalGrid,
                getItemsByExactNames(
                    listOf(
                        "Refined Metal",
                        "Reclaimed Metal",
                        "Scrap Metal"
                    )
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()

        view?.let {
            setupSteamWarning(it)
            setupMfaWarning(it)
        }
    }

    private fun setupMfaWarning(view: View) {
        val warningCard: View = view.findViewById(R.id.mfa_setup_warning)
        val configureButton: Button = view.findViewById(R.id.mfa_setup_button)

        warningCard.visibility = if (!userDatasource.hasMFA) {
            View.VISIBLE
        } else {
            View.GONE
        }

        configureButton.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            intent.putExtra("open_tab", "mfa")
            startActivity(intent)
        }
    }

    private fun setupSteamWarning(view: View) {
        val warningCard: View = view.findViewById(R.id.steam_setup_warning)
        val configureButton: Button = view.findViewById(R.id.steam_setup_button)

        val missingSteamSetup =
            userDatasource.steamId == 0L ||
                    userDatasource.tradeUrl.isBlank()

        warningCard.visibility = if (missingSteamSetup) {
            View.VISIBLE
        } else {
            View.GONE
        }

        configureButton.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            intent.putExtra("open_tab", "steam")
            startActivity(intent)
        }
    }

    private fun getMostValuableByQuality(
        quality: Quality
    ): List<HomeSteamItem> {
        val priceCache = backpackDatasource.prices ?: return emptyList()

        return priceCache.items
            .filter { entry ->
                entry.value.prices.containsKey(quality)
            }
            .filterNot { entry ->
                isExcludedShowcaseItem(entry.key)
            }
            .map { entry ->
                HomeSteamItem(
                    item = entry.value,
                    value = getHighestItemValueForQuality(entry.value, quality)
                )
            }
            .filter { item ->
                item.value > 0
            }
            .sortedByDescending { item ->
                item.value
            }
            .take(6)
    }

    private fun getItemsByExactNames(
        names: List<String>
    ): List<HomeSteamItem> {
        val priceCache = backpackDatasource.prices ?: return emptyList()

        return names.mapNotNull { name ->
            val item = priceCache.items[name] ?: return@mapNotNull null

            HomeSteamItem(
                item = item,
                value = getHighestItemValue(item)
            )
        }
    }

    private fun getHighestItemValueForQuality(
        item: Item,
        quality: Quality
    ): Int {
        val itemPair = item.prices[quality] ?: return 0

        return (itemPair.craftable.values + itemPair.uncraftable.values)
            .maxOrNull() ?: 0
    }

    private fun getHighestItemValue(item: Item): Int {
        return item.prices.values
            .flatMap { itemPair ->
                itemPair.craftable.values + itemPair.uncraftable.values
            }
            .maxOrNull() ?: 0
    }

    private fun isExcludedShowcaseItem(name: String): Boolean {
        return name.contains("Strangifier", ignoreCase = true) ||
                name.contains("Upgrade to Premium", ignoreCase = true)
    }

    private fun addItemsToGrid(
        grid: GridLayout,
        items: List<HomeSteamItem>
    ) {
        grid.removeAllViews()

        val visibleItems = items.take(6)

        visibleItems.forEach { item ->
            val itemView = layoutInflater.inflate(
                R.layout.card_item,
                grid,
                false
            )

            val icon: ImageView = itemView.findViewById(R.id.weapon_icon)
            val name: TextView = itemView.findViewById(R.id.weapon_name)

            name.text = item.item.marketHashName
            icon.contentDescription = item.item.icon

            Glide.with(requireContext())
                .load(item.item.icon)
                .placeholder(R.drawable.item_not_found)
                .error(R.drawable.item_not_found)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(icon)

            itemView.setOnClickListener {
                val dialog = ItemDialogFragment(
                    item.item
                )

                dialog.show(parentFragmentManager, null)
            }

            itemView.layoutParams = createGridParams()
            grid.addView(itemView)
        }

        repeat((3 - visibleItems.size % 3) % 3) {
            val placeholder = View(requireContext())
            placeholder.layoutParams = createGridParams()
            placeholder.visibility = View.INVISIBLE
            grid.addView(placeholder)
        }
    }

    private fun createGridParams(): GridLayout.LayoutParams {
        val cardHeight = (150 * resources.displayMetrics.density).toInt()

        return GridLayout.LayoutParams().apply {
            width = 0
            height = cardHeight
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(6, 6, 6, 6)
        }
    }
}