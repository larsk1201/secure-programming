package com.nhlstenden.guineatrade.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.datasources.UserDatasource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserFragment : Fragment() {

    @Inject
    lateinit var userDatasource: UserDatasource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailInput = view.findViewById<EditText>(R.id.settings_input_email)
        val currentPasswordInput = view.findViewById<EditText>(R.id.settings_input_current_password)
        val newPasswordInput = view.findViewById<EditText>(R.id.settings_input_password)
        val confirmPasswordInput = view.findViewById<EditText>(R.id.settings_input_password_confirm)
        val mfaCodeInput = view.findViewById<EditText>(R.id.settings_input_mfa_code)
        val mfaText = view.findViewById<TextView>(R.id.settings_mfa_textview)

        if (this.userDatasource.hasMFA) {
            mfaCodeInput.visibility = View.VISIBLE
            mfaText.visibility = View.VISIBLE
        }

        emailInput.text = this.userDatasource.email
    }
}