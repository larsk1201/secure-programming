package com.nhlstenden.guineatrade

import android.os.Bundle
import android.util.Log
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
            // Do not set user data in fragment arguments; rely on AuthRepository
            return fragment
        }
    }
}
