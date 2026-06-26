package com.nhlstenden.guineatrade.activities

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CheckoutFragment())
                .commit()
        }

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

        currentPage = checkoutPage

        val fragment = when (checkoutPage) {
            CheckoutPage.CART -> CheckoutFragment()
            CheckoutPage.AWAITING_TRADE -> CheckoutFragmentTradeSent()
            CheckoutPage.PAYMENT -> CheckoutFragmentPayment(data)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    enum class CheckoutPage(val position: Int) {
        CART(0),
        AWAITING_TRADE(1),
        PAYMENT(2)
    }
}