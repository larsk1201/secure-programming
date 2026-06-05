package com.nhlstenden.guineatrade.fragments

import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SteamFragment : Fragment() {

    @Inject
    lateinit var userDatasource: UserDatasource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_settings_steam,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val steamIdInput =
            view.findViewById<EditText>(R.id.settings_input_steamid)

        val tradeUrlInput =
            view.findViewById<EditText>(R.id.settings_input_tradeurl)

        val saveButton =
            view.findViewById<Button>(R.id.button_save_steam)

        saveButton.setOnClickListener {

            val steamIdText = steamIdInput.text.toString()
            val steamId = steamIdText.toLongOrNull()

            if (steamId == null) {
                Toast.makeText(context, "Invalid Steam ID", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val tradeUrl =
                tradeUrlInput.text.toString()

            lifecycleScope.launch {

                if (!userDatasource.updateSteam(
                        steamId,
                        tradeUrl
                    )
                ) {
                    Toast.makeText(
                        context,
                        "Unable to save Steam settings",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                Toast.makeText(
                    context,
                    "Steam settings saved",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}