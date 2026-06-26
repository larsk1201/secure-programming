package com.nhlstenden.guineatrade.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.activities.LoginActivity
import com.nhlstenden.guineatrade.datasources.SettingsDatasource
import com.nhlstenden.guineatrade.datasources.SettingsKeys
import com.nhlstenden.guineatrade.datasources.UserDatasource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class AppFragment : Fragment() {

    @Inject lateinit var userDatasource: UserDatasource
    @Inject lateinit var settingsDatasource: SettingsDatasource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val biometricEnabledSwitch = view.findViewById<SwitchCompat>(R.id.require_biometric_login_switch)
        val autoLoginSwitch = view.findViewById<SwitchCompat>(R.id.automatic_login_switch)
        val logoutEverywhereButton = view.findViewById<Button>(R.id.logout_everywhere_button)
        val logoutButton = view.findViewById<Button>(R.id.logout_button)

//        Load settings from SettingsDatasource
        lifecycleScope.launch {
            biometricEnabledSwitch.isChecked = async {
                this@AppFragment.settingsDatasource.load(SettingsKeys.BIOMETRIC_ENABLED, false)
            }.await()
            autoLoginSwitch.isChecked = async {
                this@AppFragment.settingsDatasource.load(SettingsKeys.AUTO_LOGIN_ENABLED, false)
            }.await()
        }

        logoutButton.setOnClickListener {
            Log.d("AppFragment", "Logged out")
            lifecycleScope.launch {
                val hasLoggedOut = async {
                    this@AppFragment.userDatasource.logout()
                }.await()
                if (hasLoggedOut) {
                    Toast.makeText(context, "Logged Out", Toast.LENGTH_LONG).show()
                    this@AppFragment.goToLogin()
                }
            }
        }

        logoutEverywhereButton.setOnClickListener {
            Log.d("AppFragment", "Logged out everywhere")
            lifecycleScope.launch {
                val hasLoggedOut = async {
                    this@AppFragment.userDatasource.logoutEverywhere()
                }.await()
                if (hasLoggedOut) {
                    Toast.makeText(context, "Logged out everywhere", Toast.LENGTH_LONG).show()
                    this@AppFragment.goToLogin()
                }
            }
        }

        autoLoginSwitch.setOnCheckedChangeListener { button, isChecked ->
            val key = if (isChecked) {
                this@AppFragment.userDatasource.tokens.refresh
            } else {
                ""
            }
            lifecycleScope.launch {
                this@AppFragment.settingsDatasource.save(SettingsKeys.AUTO_LOGIN_ENABLED, isChecked)
                this@AppFragment.settingsDatasource.save(SettingsKeys.LOGIN_KEY, key ?: "")
            }
        }

        biometricEnabledSwitch.setOnCheckedChangeListener { button, isChecked ->
            if (!isChecked) {
                lifecycleScope.launch {
                    this@AppFragment.settingsDatasource.save(SettingsKeys.BIOMETRIC_ENABLED, false)
                    Toast.makeText(context, "You have disabled Biometric authentication", Toast.LENGTH_LONG).show()
                }
                return@setOnCheckedChangeListener
            }

            val biometricManager = BiometricManager.from(requireContext())
            when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    lifecycleScope.launch {
                        this@AppFragment.settingsDatasource.save(SettingsKeys.BIOMETRIC_ENABLED, true)
                        Toast.makeText(context, "You have enabled Biometric authentication", Toast.LENGTH_LONG).show()
                    }
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    Toast.makeText(context, "No biometric features available on this device.", Toast.LENGTH_LONG).show()
                    button.isChecked = false
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    Toast.makeText(context, "Biometric features are currently unavailable.", Toast.LENGTH_LONG).show()
                    button.isChecked = false
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    Toast.makeText(context,  "No biometric authentication enrolled.", Toast.LENGTH_LONG).show()
                    button.isChecked = false
                }
                else -> {
                    Toast.makeText(context, "Unknown error", Toast.LENGTH_LONG).show()
                    button.isChecked = false
                }
            }
        }
    }

    private fun goToLogin() {
        val intent = Intent(context, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        activity?.finish()
    }
}