package com.nhlstenden.guineatrade.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.BackpackDatasource
import com.nhlstenden.guineatrade.datasources.CartDatasource
import com.nhlstenden.guineatrade.datasources.CartItem
import com.nhlstenden.guineatrade.utils.Pricing
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.nhlstenden.guineatrade.activities.CheckoutActivity
import com.nhlstenden.guineatrade.datasources.ButtonColor
import com.nhlstenden.guineatrade.datasources.CartItemType
import com.nhlstenden.guineatrade.datasources.Price

@AndroidEntryPoint
class CheckoutFragment : Fragment() {

    @Inject
    lateinit var cartDatasource: CartDatasource
    @Inject
    lateinit var backpackDatasource: BackpackDatasource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_checkout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val totalPrice = view.findViewById<TextView>(R.id.price)
        val cartListing = view.findViewById<ListView>(R.id.cart_listing)
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)
        val clearButton = view.findViewById<Button>(R.id.clear_button)

        val adapter = CartAdapter(
            requireContext(),
            ArrayList(cartDatasource.cart.value)
        ) { it.getPrice(backpackDatasource) }

        cartListing.adapter = adapter

        fun setupView() {
            val price = cartDatasource.getTotalPrice()
            totalPrice.text = buildString {
                append((if (price.type == CartItemType.SELL) "Selling for: " else "Buying for: "))
                append(Pricing.toFormattedPriceString(price.price))
            }

            if (cartDatasource.cart.value.isNotEmpty() && Pricing.isTradeAllowed(price)) {
                confirmButton.setBackgroundColor(ButtonColor.HAS_STOCK.toColour(view.context))
                confirmButton.setOnClickListener {
                    lifecycleScope.launch {
                        val response = cartDatasource.createPaymentRequest()
                        Log.d("CheckoutFragment", response.toString())
                        if (response != null) {
                            when (response.status) {
                                "created_link" -> {
                                    (requireActivity() as CheckoutActivity)
                                        .navigateTo(CheckoutActivity.CheckoutPage.PAYMENT,response.url ?: "" )
                                }
                                "no_payment_required" -> {
                                    (requireActivity() as CheckoutActivity)
                                        .navigateTo(CheckoutActivity.CheckoutPage.AWAITING_TRADE)
                                }
                            }
                            cartDatasource.clearCart()
                        } else {
                            Toast.makeText(context, "Unable to process cart, try again later", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                confirmButton.setBackgroundColor(ButtonColor.OUT_OF_STOCK.toColour(view.context))
            }
        }

        setupView()

        clearButton.setOnClickListener {
            Log.d("CheckoutFragment", "Cleared cart")
            cartDatasource.clearCart()
            val adapter = CartAdapter(
                requireContext(),
                ArrayList(cartDatasource.cart.value)
            ) { it.getPrice(backpackDatasource) }

            cartListing.adapter = adapter

            adapter.notifyDataSetChanged()
            setupView()
        }
    }

    class CartAdapter(
        context: Context,
        items: ArrayList<CartItem>,
        private val getItemPrice: (CartItem) -> Price
    ) : ArrayAdapter<CartItem>(context, 0, items) {
        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            var itemView = view
            if (itemView == null) {
                itemView = LayoutInflater.from(context).inflate(R.layout.card_item_checkout, parent, false)
            }

            val item = getItem(position)!!
            val itemName = itemView!!.findViewById<TextView>(R.id.item_name)
            val itemPrice = itemView.findViewById<TextView>(R.id.item_price)
            val itemQualityCard = itemView.findViewById<CardView>(R.id.item_card)
            val itemQualityName = itemView.findViewById<TextView>(R.id.item_quality_name)
            val itemEffectCard = itemView.findViewById<CardView>(R.id.item_card_effect)
            val itemEffectName = itemView.findViewById<TextView>(R.id.item_effect_name)
            val itemBuySell = itemView.findViewById<TextView>(R.id.item_buy_sell)
            val itemImage = itemView.findViewById<ImageView>(R.id.item_image)

            val price = getItemPrice(item)

            itemName.text = context.getString(
                R.string.cart_item_name,
                item.stock.item.marketHashName,
                item.stock.quantity
            )
            itemPrice.text = Pricing.toFormattedPriceString(price.price)
            itemQualityCard.setCardBackgroundColor(item.stock.item.quality.toColour(context))
            itemQualityName.text = item.stock.item.quality.name
            itemBuySell.text = if (item.type == CartItemType.SELL) "SELLING" else "BUYING"

            val unusual = item.stock.item.unusual
            if ( !unusual.isNullOrBlank() && unusual != "0") {
                itemEffectName.text = unusual
            } else {
                itemEffectCard.visibility = View.INVISIBLE
            }

            Glide.with(context)
                .load(item.imageUrl)
                .placeholder(R.drawable.item_not_found)
                .error(R.drawable.item_not_found)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(itemImage)

            return itemView
        }
    }
}