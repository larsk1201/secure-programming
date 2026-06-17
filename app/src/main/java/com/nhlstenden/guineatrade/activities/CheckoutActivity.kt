package com.nhlstenden.guineatrade.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.fragments.CheckoutFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CheckoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val viewPager: ViewPager2 = findViewById(R.id.view_pager)

        viewPager.adapter = CheckoutPageAdapter(this)
        viewPager.isUserInputEnabled = false
    }

    private class CheckoutPageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            val fragment = when (CheckoutPage.entries[position]) {
                CheckoutPage.CART -> CheckoutFragment()
            }

            return fragment
        }
    }

    enum class CheckoutPage(val position: Int) {
        CART(0),
    }
}