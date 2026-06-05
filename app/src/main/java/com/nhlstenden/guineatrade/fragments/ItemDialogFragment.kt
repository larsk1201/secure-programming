package com.nhlstenden.guineatrade.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import androidx.fragment.app.DialogFragment
import com.nhlstenden.guineatrade.R

class ItemDialogFragment(val gridViewModel: GridViewModel): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = context?.getSystemService<LayoutInflater>()
        var view = inflater?.inflate(R.layout.popup_item_details, null)

        return AlertDialog.Builder(requireContext())
            .setView(R.layout.popup_item_details)
            .create()
    }
}