package com.nhlstenden.guineatrade.activities

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.CartDatasource
import com.nhlstenden.guineatrade.fragments.CheckoutFragment
import com.nhlstenden.guineatrade.fragments.CheckoutFragmentPayment
import com.nhlstenden.guineatrade.fragments.CheckoutFragmentTradeSent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CheckoutActivity : AppCompatActivity() {
    @Inject lateinit var cartDatasource: CartDatasource
    private var currentPage: CheckoutPage = CheckoutPage.CART
    private var tradePollingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val pager = findViewById<ViewPager2>(R.id.view_page)
        pager.adapter = CheckoutPagerAdapter(this, "")
        pager.isUserInputEnabled = false
        pager.setCurrentItem(CheckoutPage.CART.position, false)

        val backButton = findViewById<Button>(R.id.back_button)

        backButton.setOnClickListener {
            this.finish()
        }
    }

    override fun onStart() {
        super.onStart()

        // Run refresh once to check for an active trade session
        lifecycleScope.launch {
            refreshTradeStatus()
        }

        startTradePolling()
    }

    override fun onStop() {
        super.onStop()
        tradePollingJob?.cancel()
    }

    private fun startTradePolling() {
        tradePollingJob?.cancel()

        tradePollingJob = lifecycleScope.launch {
            while (true) {
                if (currentPage != CheckoutPage.CART) {
                    refreshTradeStatus()
                }
                delay(10000)
            }
        }
    }

    suspend fun refreshTradeStatus() {
        val tradeStatus = cartDatasource.getTradeStatus() ?: return
        val page = when (tradeStatus.status) {
            CartDatasource.TradeStatusType.PAYMENT_IN_PROGRESS -> CheckoutPage.PAYMENT
            CartDatasource.TradeStatusType.TRADE_IN_PROGRESS -> {
                if (currentPage == CheckoutPage.PAYMENT) {
                    Toast.makeText(this@CheckoutActivity, "Payment completed!", Toast.LENGTH_SHORT).show()
                }
                CheckoutPage.AWAITING_TRADE
            }
            CartDatasource.TradeStatusType.COMPLETED -> {
                Toast.makeText(this@CheckoutActivity, "Trade completed!", Toast.LENGTH_SHORT).show()
                CheckoutPage.CART
            }
            CartDatasource.TradeStatusType.CANCELLED -> {
                Toast.makeText(this@CheckoutActivity, "Trade cancelled", Toast.LENGTH_SHORT).show()
                CheckoutPage.CART
            }
            else -> CheckoutPage.CART
        }

        navigateTo(page, tradeStatus.data)
    }

    fun navigateTo(checkoutPage: CheckoutPage, data: String = "") {
        if (checkoutPage == currentPage) return

        val pager = findViewById<ViewPager2>(R.id.view_page)
        pager.adapter = CheckoutPagerAdapter(this,  data)
        pager.isUserInputEnabled = false
        pager.setCurrentItem(checkoutPage.position, false)
    }

    enum class CheckoutPage(val position: Int) {
        CART(0),
        AWAITING_TRADE(1),
        PAYMENT(2)
    }

    class CheckoutPagerAdapter(fragmentActivity: FragmentActivity, val data: String = "") : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = CheckoutPage.entries.size

        override fun createFragment(position: Int): Fragment {
            return when (CheckoutPage.entries[position]) {
                CheckoutPage.CART -> CheckoutFragment()
                CheckoutPage.AWAITING_TRADE -> CheckoutFragmentTradeSent()
                CheckoutPage.PAYMENT -> CheckoutFragmentPayment().apply {
                    arguments = Bundle().apply {
                        putString("paymentUrl", data)
                    }
                }
            }
        }
    }
}