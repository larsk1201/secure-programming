package com.nhlstenden.guineatrade.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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

        val mfaCodeInput = view.findViewById<EditText>(R.id.settings_input_mfa_code)
        val mfaText = view.findViewById<TextView>(R.id.settings_mfa_textview)

        val cancelButton = view.findViewById<Button>(R.id.settings_button_cancel)
        val confirmButton = view.findViewById<Button>(R.id.settings_button_confirm)

        if (this.userDatasource.hasMFA) {
            mfaCodeInput.visibility = View.VISIBLE
            mfaText.visibility = View.VISIBLE
        }

        cancelButton.setOnClickListener {
            activity?.finish()
        }

        confirmButton.setOnClickListener {
            val email = view.findViewById<EditText>(R.id.settings_input_email).text.toString()
            val currentPassword = view.findViewById<EditText>(R.id.settings_input_current_password).text.toString()
            val newPassword = view.findViewById<EditText>(R.id.settings_input_password).text.toString()
            val confirmPassword = view.findViewById<EditText>(R.id.settings_input_password_confirm).text.toString()

            if (email.isEmpty() || currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isValidEmail(email)) {
                Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPassword != confirmPassword) {
                Toast.makeText(context, "Passwords does not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var totpCode =  ""
            if (this.userDatasource.hasMFA) {
                totpCode = mfaCodeInput.text.toString()
                if (totpCode.isEmpty()) {
                    Toast.makeText(context, "OTP field is empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            lifecycleScope.launch {
                if (!this@UserFragment.userDatasource.updateMe(
                    totpCode,
                    currentPassword,
                    newPassword,
                    confirmPassword,
                )) {
                    Toast.makeText(context, "Wrong email or password, unable to update", Toast.LENGTH_LONG).show()
                    return@launch
                }
                Toast.makeText(context, "Succesfully updated your password", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}