package com.nhlstenden.guineatrade.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.UserDatasource
import com.nhlstenden.guineatrade.fragments.AppFragment
import com.nhlstenden.guineatrade.fragments.HomeFragment
import com.nhlstenden.guineatrade.fragments.InventoryFragment
import com.nhlstenden.guineatrade.fragments.MfaFragment
import com.nhlstenden.guineatrade.fragments.StoreFragment
import com.nhlstenden.guineatrade.fragments.UserFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity: AppCompatActivity() {

    @Inject lateinit var userDatasource: UserDatasource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val settingsNavigation: BottomNavigationView = findViewById(R.id.profile_navigation)
        val viewPager: ViewPager2 = findViewById(R.id.profile_viewpager)

        viewPager.adapter = ProfilePageAdapter(this)

        settingsNavigation.setOnItemSelectedListener { item ->
            viewPager.currentItem = when (item.itemId) {
                R.id.nav_user -> ProfilePage.USER.position
                R.id.nav_otp -> ProfilePage.MFA.position
                R.id.nav_app -> ProfilePage.APP.position
                else -> ProfilePage.USER.position
            }

            true
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                settingsNavigation.selectedItemId = when (ProfilePage.entries[position]) {
                    ProfilePage.USER -> R.id.nav_user
                    ProfilePage.MFA -> R.id.nav_otp
                    ProfilePage.APP -> R.id.nav_app
                }
            }
        })
    }
    
    private class ProfilePageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            val fragment = when (ProfilePage.entries[position]) {
                ProfilePage.USER -> UserFragment()
                ProfilePage.MFA -> MfaFragment()
                ProfilePage.APP -> AppFragment()
            }

            return fragment
        }
    }

    enum class ProfilePage(val position: Int) {
        USER(0),
        MFA(1),
        APP(2),
    }
}