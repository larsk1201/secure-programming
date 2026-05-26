package com.nhlstenden.guineatrade.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.activities.MainActivity
import com.nhlstenden.guineatrade.datasources.SettingsDatasource
import com.nhlstenden.guineatrade.datasources.SettingsKeys
import com.nhlstenden.guineatrade.datasources.Tokens
import com.nhlstenden.guineatrade.datasources.UserDatasource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@AndroidEntryPoint
class LoginFragment : Fragment() {

    @Inject lateinit var userDatasource: UserDatasource
    @Inject lateinit var settingsDatasource: SettingsDatasource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            val autoLoginEnabled = this@LoginFragment.settingsDatasource.load(SettingsKeys.AUTO_LOGIN_ENABLED, false)
            val biometricLoginEnabled = this@LoginFragment.settingsDatasource.load(SettingsKeys.BIOMETRIC_ENABLED, false)

            if (!autoLoginEnabled) {
                return@launch
            }
            if (biometricLoginEnabled) {
                if (!biometricLogin()) {
                    return@launch
                }
            }

            val refreshToken = this@LoginFragment.settingsDatasource.load(SettingsKeys.LOGIN_KEY, "")
            this@LoginFragment.userDatasource.tokens = Tokens("", refreshToken)

            if (!this@LoginFragment.userDatasource.refreshToken()) {
                Toast.makeText(context, "Expired token, unable to login", Toast.LENGTH_SHORT).show()
                return@launch
            }


            val intent = Intent()
            intent.setClass(requireContext(), MainActivity::class.java)
            activity?.startActivity(intent)
        }

        val submitButton = view.findViewById<Button>(R.id.login_button_confirm)

        submitButton.setOnClickListener {
            val email = view.findViewById<EditText>(R.id.login_email).text.toString()
            val password = view.findViewById<EditText>(R.id.login_password).text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                if (!this@LoginFragment.userDatasource.login(email, password)) {
                    Toast.makeText(context, "Invalid credentials, unable to login", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val intent = Intent()
                intent.setClass(requireContext(), MainActivity::class.java)
                activity?.startActivity(intent)
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private suspend fun biometricLogin(): Boolean = suspendCancellableCoroutine { continuation ->
        val executor = ContextCompat.getMainExecutor(requireContext())
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(context,
                        "Authentication error: $errString", Toast.LENGTH_SHORT)
                        .show()
                    continuation.resume(false)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(context,
                        "Authentication succeeded!", Toast.LENGTH_SHORT)
                        .show()
                    continuation.resume(true)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(context, "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login with fingerprint")
            .setSubtitle("Log into GuineaTrade using your fingerprint scanner")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}