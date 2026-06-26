package com.nhlstenden.guineatrade.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.nhlstenden.guineatrade.R
import androidx.core.net.toUri

class CheckoutFragmentPayment(): Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_checkout_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toPaymentButton = view.findViewById<Button>(R.id.pay_button)

        val paymentUrl = arguments?.getString("paymentUrl") ?: ""
        Log.d("CheckoutFragmentPayment", paymentUrl)
        toPaymentButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, paymentUrl.toUri())
            startActivity(browserIntent)
        }
    }
}