package com.nhlstenden.guineatrade.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.nhlstenden.guineatrade.R

class SignupFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val submitButton = view.findViewById<Button>(R.id.login_button_confirm)

        submitButton.setOnClickListener {
            val email = view.findViewById<EditText>(R.id.signup_input_email).text.toString()
            val password = view.findViewById<EditText>(R.id.signup_input_password).text.toString()
            val passwordConfirm = view.findViewById<EditText>(R.id.signup_input_password_confirm).text.toString()
            val phoneNumber = view.findViewById<EditText>(R.id.signup_phone_number).text.toString()


            if (email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty() || phoneNumber.isEmpty()) {
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

//            TODO: Login the user (singleton is currently nil)
//            this.user.signup(email, password, passwordConfirm, phone)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}