package com.nhlstenden.guineatrade.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.UserDatasource
import dagger.hilt.android.AndroidEntryPoint
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

        val button = view.findViewById<Button>(R.id.button)
        val setupPrompt = view.findViewById<LinearLayout>(R.id.setup_prompt)

        button.setOnClickListener {
            setupPrompt.visibility = View.VISIBLE
        }

        val totpTokenText = view.findViewById<TextView>(R.id.totp_token)
        val copyTotpTokenButton = view.findViewById<ImageButton>(R.id.totp_token_copy)
        copyTotpTokenButton.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("GuineaTrade TOTP token", totpTokenText.text.toString())
            clipboard.setPrimaryClip(clipData)
        }

        val recoveryTokenText = view.findViewById<TextView>(R.id.totp_recovery_code)
        val copyRecoveryTokenButton = view.findViewById<ImageButton>(R.id.totp_recovery_code_copy)
        copyRecoveryTokenButton.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("GuineaTrade TOTP token", recoveryTokenText.text.toString())
            clipboard.setPrimaryClip(clipData)
        }

        val finishButton = view.findViewById<Button>(R.id.mfa_finish_setup)
        finishButton.setOnClickListener {
//            TODO: API to verify code
        }

        val cancelButton = view.findViewById<Button>(R.id.mfa_cancel_setup)
        cancelButton.setOnClickListener {
//            TODO: API to reset code
        }
    }
}