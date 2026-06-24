package com.nhlstenden.guineatrade.fragments

import android.content.Context
import android.content.Intent
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
import androidx.core.net.toUri
import com.nhlstenden.guineatrade.activities.MainActivity
import com.nhlstenden.guineatrade.datasources.ButtonColor
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

        val totalPrice = view.findViewById<TextView>(R.id.last_update)
        val cartListing = view.findViewById<ListView>(R.id.cart_listing)
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)
        val clearButton = view.findViewById<Button>(R.id.clear_button)
        val goBackButton = view.findViewById<Button>(R.id.back_button2)

        val adapter = CartAdapter(
            requireContext(),
            ArrayList(cartDatasource.cart.value)
        ) { it.getPrice(backpackDatasource) }

        cartListing.adapter = adapter

        fun setupView() {
            val price = cartDatasource.getTotalPrice()
            totalPrice.text = Pricing.toFormattedPriceString(price.price)

            if (cartDatasource.cart.value.size > 0 && Pricing.isTradeAllowed(price)) {
                confirmButton.setBackgroundColor(ButtonColor.HAS_STOCK.toColour(view.context))
                confirmButton.setOnClickListener {
                    lifecycleScope.launch {
                        val response = cartDatasource.createPaymentRequest()
                        Log.d("CheckoutFragment", response.toString())
                        if (response != null) {
                            when (response.status) {
                                "created_link" -> {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = response.url!!.toUri()
                                    }

                                    startActivity(intent)
                                }
                                "no_payment_required" -> {

                                }
                            }
                        }
                    }
                    Log.d("CheckoutFragment", "Navigate to payment")
                    // TODO: navigate to payment or trading screen
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

        goBackButton.setOnClickListener {
            val intent = Intent()
            intent.setClass(view.context, MainActivity::class.java)
            startActivity(intent)
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
            val itemQuantity = itemView.findViewById<TextView>(R.id.item_quantity)
            val itemImage = itemView.findViewById<ImageView>(R.id.item_image)

            val price = getItemPrice(item)

            itemName.text = "${item.stock.item.marketHashName} ${item.stock.item.quality}"
            itemPrice.text = Pricing.toFormattedPriceString(price.price)
            itemQuantity.text = "x${item.stock.quantity}"

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