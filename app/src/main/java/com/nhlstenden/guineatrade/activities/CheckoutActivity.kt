package com.nhlstenden.guineatrade.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.fragments.CheckoutFragment
import com.nhlstenden.guineatrade.fragments.CheckoutFragmentPayment
import com.nhlstenden.guineatrade.fragments.CheckoutFragmentTradeSent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CheckoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CheckoutFragment())
                .commit()
        }
    }

    fun navigateTo(checkoutPage: CheckoutPage, paymentUrl: String = "") {
        val fragment = when (checkoutPage) {
            CheckoutPage.CART -> CheckoutFragment()
            CheckoutPage.AWAITING_TRADE -> CheckoutFragmentTradeSent()
            CheckoutPage.PAYMENT -> CheckoutFragmentPayment(paymentUrl)

        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(fragment::class.java.simpleName)
            .commit()
    }

    enum class CheckoutPage(val position: Int) {
        CART(0),
        AWAITING_TRADE(1),
        PAYMENT(2)
    }
}