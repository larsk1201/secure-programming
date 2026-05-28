package com.nhlstenden.guineatrade.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.UserDatasource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MfaDisabledFragment : Fragment() {

    @Inject
    lateinit var userDatasource: UserDatasource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_mfa_disabled, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startSetupButton = view.findViewById<Button>(R.id.start_setup_button)
        val setupPrompt = view.findViewById<LinearLayout>(R.id.setup_prompt)

        val totpTokenText = view.findViewById<TextView>(R.id.totp_token)
        val copyTotpTokenButton = view.findViewById<ImageButton>(R.id.totp_token_copy)

        val recoveryTokenText = view.findViewById<TextView>(R.id.totp_recovery_code)
        val copyRecoveryTokenButton = view.findViewById<ImageButton>(R.id.totp_recovery_code_copy)

        val totpTokenInput = view.findViewById<EditText>(R.id.totp_code)

        startSetupButton.setOnClickListener {
            lifecycleScope.launch {
                val totpToken = userDatasource.registerTOTPToken()
                if (totpToken.isSuccess) {
                    val tokens = totpToken.getOrNull()
                    setupPrompt.visibility = View.VISIBLE

                    totpTokenText.text = tokens?.code
                    recoveryTokenText.text = tokens?.recovery
                } else {
                    Toast.makeText(context, "${totpToken.exceptionOrNull()}", Toast.LENGTH_LONG).show()
                }
            }
        }

        copyTotpTokenButton.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("GuineaTrade TOTP token", totpTokenText.text.toString())
            clipboard.setPrimaryClip(clipData)
        }

        copyRecoveryTokenButton.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("GuineaTrade TOTP token", recoveryTokenText.text.toString())
            clipboard.setPrimaryClip(clipData)
        }

        val finishButton = view.findViewById<Button>(R.id.mfa_finish_setup)
        finishButton.setOnClickListener {
            lifecycleScope.launch {
                val success = userDatasource.validateTOTPCode(totpTokenInput.text.toString())
                if (success) {
                    Toast.makeText(context, "Successfully registered MFA", Toast.LENGTH_LONG).show()
                    userDatasource.authMe()
                } else {
                    Toast.makeText(context, "Failed to register MFA, try again", Toast.LENGTH_LONG).show()
                }
            }
        }

        val cancelButton = view.findViewById<Button>(R.id.mfa_cancel_setup)
        cancelButton.setOnClickListener {
            setupPrompt.visibility = View.GONE
            lifecycleScope.launch {
                val success = userDatasource.deactivateTOTPCode(recoveryTokenText.text.toString(), true)
                if (success) {
                    Toast.makeText(context, "Successfully deleted MFA", Toast.LENGTH_LONG).show()
                    userDatasource.authMe()
                } else {
                    Toast.makeText(context, "Failed to delete MFA, try again", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}