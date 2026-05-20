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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nhlstenden.guineatrade.R
import com.nhlstenden.guineatrade.activities.MainActivity
import com.nhlstenden.guineatrade.datasources.UserDatasource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SignupFragment : Fragment() {

    @Inject
    lateinit var userDatasource: UserDatasource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val submitButton = view.findViewById<Button>(R.id.signup_button_confirm)

        submitButton.setOnClickListener {
            val name = view.findViewById<EditText>(R.id.signup_input_username).text.toString()
            val email = view.findViewById<EditText>(R.id.signup_input_email).text.toString()
            val password = view.findViewById<EditText>(R.id.signup_input_password).text.toString()
            val passwordConfirm = view.findViewById<EditText>(R.id.signup_input_password_confirm).text.toString()
            val phoneNumber = view.findViewById<EditText>(R.id.signup_phone_number).text.toString()


            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordConfirm) {
                Toast.makeText(context, "Passwords does not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
//            TODO: Implement
//            if (!phoneNumber.isvalid()) {
//                Toast.makeText(context, "Phone number is not valid", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }

            lifecycleScope.launch {
                if (!this@SignupFragment.userDatasource.signup(name, email, password, passwordConfirm, phoneNumber)) {
                    Toast.makeText(context, "Unable to create new account", Toast.LENGTH_SHORT).show()
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
}