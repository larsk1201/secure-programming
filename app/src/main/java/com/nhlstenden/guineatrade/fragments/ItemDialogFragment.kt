package com.nhlstenden.guineatrade.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nhlstenden.guineatrade.R

class ItemDialogFragment(val gridViewModel: GridViewModel): DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.popup_item_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val weaponText = view.findViewById<TextView>(R.id.item_popup_name)
        val weaponIcon = view.findViewById<ImageView>(R.id.item_popup_icon)

        weaponText.text = gridViewModel.itemName

        Glide.with(requireContext())
            .load(gridViewModel.imageUrl)
            .placeholder(R.drawable.item_not_found)
            .error(R.drawable.item_not_found)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(weaponIcon)

        view.findViewById<Button>(R.id.item_popup_ok).setOnClickListener {
            this@ItemDialogFragment.dismiss()
        }

        view.findViewById<Button>(R.id.item_popup_cancel).setOnClickListener {
            this@ItemDialogFragment.dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = getDialog()
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }
}