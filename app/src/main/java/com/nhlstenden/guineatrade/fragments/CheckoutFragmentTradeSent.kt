package com.nhlstenden.guineatrade.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.activities.MainActivity

class CheckoutFragmentTradeSent : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_checkout_trade_sent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val goBackButton = view.findViewById<Button>(R.id.back_button3)

        goBackButton.setOnClickListener {
            val intent = Intent()
            intent.setClass(view.context, MainActivity::class.java)
            startActivity(intent)
        }
    }
}