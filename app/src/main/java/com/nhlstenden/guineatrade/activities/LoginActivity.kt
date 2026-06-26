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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.login_navigation)
        val viewPager: ViewPager2 = findViewById(R.id.login_viewpage)

        viewPager.adapter = LoginPageAdapter(this)

        bottomNavigation.setOnItemSelectedListener { item ->
            viewPager.currentItem = when (item.itemId) {
                R.id.nav_login -> LoginPage.LOGIN.position
                R.id.nav_signup -> LoginPage.SIGNUP.position
                else -> LoginPage.LOGIN.position
            }

            true
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNavigation.selectedItemId = when (LoginPage.entries[position]) {
                    LoginPage.LOGIN -> R.id.nav_login
                    LoginPage.SIGNUP -> R.id.nav_signup
                }
            }
        })
    }

    private class LoginPageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            val fragment = when (LoginPage.entries[position]) {
                LoginPage.LOGIN -> LoginFragment()
                LoginPage.SIGNUP -> SignupFragment()
            }

            return fragment
        }
    }

    enum class LoginPage(val position: Int) {
        LOGIN(0),
        SIGNUP(1)
    }
}