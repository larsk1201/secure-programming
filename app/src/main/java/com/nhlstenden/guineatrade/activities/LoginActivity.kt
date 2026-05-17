package com.nhlstenden.guineatrade.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.fragments.LoginFragment
import com.nhlstenden.guineatrade.fragments.SignupFragment

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val viewPager: ViewPager2 = findViewById(R.id.view_page)

        viewPager.adapter = LoginPageAdapter(this)

        bottomNavigation.setOnItemSelectedListener { item ->
            viewPager.currentItem = when (item.itemId) {
                R.id.nav_login -> 0
                R.id.nav_signup -> 1
                else -> 0
            }

            true
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNavigation.selectedItemId = when (position) {
                    0 -> R.id.nav_login
                    1 -> R.id.nav_signup
                    else -> R.id.nav_signup
                }
            }
        })
    }

    private class LoginPageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            val fragment = when (position) {
                0 -> LoginFragment()
                1 -> SignupFragment()
                else -> LoginFragment()
            }

            return fragment
        }
    }
}