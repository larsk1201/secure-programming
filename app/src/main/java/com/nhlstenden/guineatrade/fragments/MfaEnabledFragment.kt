package com.nhlstenden.guineatrade.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.UserDatasource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MfaEnabledFragment : Fragment() {

    @Inject
    lateinit var userDatasource: UserDatasource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_mfa_enabled, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val otpCodeForm = view.findViewById<EditText>(R.id.mfa_enabled_totp_code)
        val recoveryCodeForm = view.findViewById<EditText>(R.id.mfa_enabled_recovery_code)
        val confirmButton = view.findViewById<Button>(R.id.mfa_enabled_confirm)
        val cancelButton = view.findViewById<Button>(R.id.mfa_enabled_cancel)

        confirmButton.setOnClickListener {
            val otpCode = otpCodeForm.text.toString()
            val recoveryCode = recoveryCodeForm.text.toString()
            if (otpCode.isEmpty() && recoveryCode.isEmpty()) {
                Toast.makeText(context, "Fill in either an OTP code or recovery code", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val isRecoveryCode = !recoveryCode.isEmpty()
            val code = if (isRecoveryCode) recoveryCode else otpCode
            lifecycleScope.launch {
                val unableToDisable = async {
                    this@MfaEnabledFragment.userDatasource.deactivateTOTPCode(code, isRecoveryCode)
                }.await()
                if (!unableToDisable) {
                    Toast.makeText(context, "Unable to deactivate MFA", Toast.LENGTH_LONG).show()
                    return@launch
                }
                userDatasource.authMe()
                Toast.makeText(context, "Deactivated MFA", Toast.LENGTH_LONG).show()
            }
        }

        cancelButton.setOnClickListener {
            activity?.finish()
        }
    }
}