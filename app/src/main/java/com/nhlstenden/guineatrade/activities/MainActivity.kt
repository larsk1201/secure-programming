package com.nhlstenden.guineatrade.activities

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.UserDatasource
import com.nhlstenden.guineatrade.fragments.HomeFragment
import com.nhlstenden.guineatrade.fragments.InventoryFragment
import com.nhlstenden.guineatrade.fragments.StoreFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var userDatasource: UserDatasource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", userDatasource.username)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val viewPager: ViewPager2 = findViewById(R.id.view_page)

        viewPager.adapter = MainPagerAdapter(this)

        bottomNavigation.setOnItemSelectedListener { item ->
            viewPager.currentItem = when (item.itemId) {
                R.id.nav_home -> MainPage.HOME.position
                R.id.nav_store -> MainPage.STORE.position
                R.id.nav_inventory -> MainPage.INVENTORY.position
                else -> MainPage.HOME.position
            }

            true
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNavigation.selectedItemId = when (MainPage.entries[position]) {
                    MainPage.HOME -> R.id.nav_home
                    MainPage.STORE -> R.id.nav_store
                    MainPage.INVENTORY -> R.id.nav_inventory
                }
            }
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewPager.currentItem == 0) {
                    finish()
                } else {
                    viewPager.currentItem = 0
                }
            }
        })

        this.setProfileCard()
    }

    private fun setProfileCard() {
        val greetingText: TextView = findViewById(R.id.user_name)
        val subText: TextView = findViewById(R.id.sub_text)

        greetingText.text = String.format(getText(R.string.profile_header_name).toString(), userDatasource.username)
        subText.text = String.format(getText(R.string.profile_header_subtitle).toString(), userDatasource.balance.toFloat() / 100.0)
    }

    private class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            val fragment = when (MainPage.entries[position]) {
                MainPage.HOME -> HomeFragment()
                MainPage.STORE -> StoreFragment()
                MainPage.INVENTORY -> InventoryFragment()
            }

            return fragment
        }
    }

    enum class MainPage(val position: Int) {
        HOME(0),
        STORE(1),
        INVENTORY(2),
    }
}