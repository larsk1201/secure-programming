package com.nhlstenden.guineatrade

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nhlstenden.guineatrade.fragments.HomeFragment
import com.nhlstenden.guineatrade.fragments.InventoryFragment
import com.nhlstenden.guineatrade.fragments.StoreFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val viewPager: ViewPager2 = findViewById(R.id.view_page)

        viewPager.adapter = MainPagerAdapter(this)

        bottomNavigation.setOnItemSelectedListener { item ->
            viewPager.currentItem = when (item.itemId) {
                R.id.nav_home -> 0
                R.id.nav_store -> 1
                R.id.nav_inventory -> 2
                else -> 0
            }

            true
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNavigation.selectedItemId = when (position) {
                    0 -> R.id.nav_home
                    1 -> R.id.nav_store
                    2 -> R.id.nav_inventory
                    else -> R.id.nav_home
                }
            }
        })

        this.setProfileCard()
    }

    private fun setProfileCard() {
        val greetingText: TextView = findViewById(R.id.user_name)
        val subText: TextView = findViewById(R.id.sub_text)

        //        TODO: Get values from API
        greetingText.text = String.format(getText(R.string.profile_header_name).toString(), "John Doe")
        subText.text = String.format(getText(R.string.profile_header_subtitle).toString(), 49550.0 / 100.0)
    }

    private class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            val fragment = when (position) {
                0 -> HomeFragment()
                1 -> StoreFragment()
                2 -> InventoryFragment()
                else -> HomeFragment()
            }

            return fragment
        }
    }
}
