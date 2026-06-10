package com.nhlstenden.guineatrade.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.UserDatasource
import com.nhlstenden.guineatrade.fragments.AppFragment
import com.nhlstenden.guineatrade.fragments.MfaDisabledFragment
import com.nhlstenden.guineatrade.fragments.MfaEnabledFragment
import com.nhlstenden.guineatrade.fragments.SteamFragment
import com.nhlstenden.guineatrade.fragments.UserFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity: AppCompatActivity() {

    @Inject lateinit var userDatasource: UserDatasource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val settingsNavigation: BottomNavigationView = findViewById(R.id.profile_navigation)
        val viewPager: ViewPager2 = findViewById(R.id.profile_viewpager)

        val profilePageAdapter = ProfilePageAdapter(this, userDatasource)
        viewPager.adapter = profilePageAdapter

        if (intent.getStringExtra("open_tab") == "steam") {
            viewPager.currentItem = ProfilePage.STEAM.position
            settingsNavigation.selectedItemId = R.id.nav_steam
        }

        settingsNavigation.setOnItemSelectedListener { item ->
            viewPager.currentItem = when (item.itemId) {
                R.id.nav_user -> ProfilePage.USER.position
                R.id.nav_otp -> ProfilePage.MFA.position
                R.id.nav_app -> ProfilePage.APP.position
                R.id.nav_steam -> ProfilePage.STEAM.position
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
                    ProfilePage.STEAM -> R.id.nav_steam
                }
            }
        })

        this.userDatasource.hasMFALiveData.observe(this) { hasMFA ->
            val otpMenuItem = settingsNavigation.menu.findItem(R.id.nav_otp)
            otpMenuItem.setIcon(R.drawable.gpp_maybe_24px)

            lifecycleScope.launch {
                delay(3000)
                otpMenuItem.setIcon(
                    if (hasMFA) R.drawable.verified_user_24px
                    else R.drawable.gpp_bad_24px
                )
//                TODO: Find proper way to refresh MFA fragment
            }
        }
    }
    
    private class ProfilePageAdapter(activity: FragmentActivity, val userDatasource: UserDatasource) : FragmentStateAdapter(activity) {
        fun getMfaFragment(): Fragment {
            return if (this.userDatasource.hasMFA) MfaEnabledFragment() else MfaDisabledFragment()
        }

        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            val fragment = when (ProfilePage.entries[position]) {
                ProfilePage.USER -> UserFragment()
                ProfilePage.MFA -> getMfaFragment()
                ProfilePage.APP -> AppFragment()
                ProfilePage.STEAM -> SteamFragment()
            }

            return fragment
        }
    }

    enum class ProfilePage(val position: Int) {
        USER(0),
        MFA(1),
        APP(2),
        STEAM(3),
    }
}