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

class InventoryItemDialogFragment(
    private val itemCounter: ItemCounter
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.inventory_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val image = view.findViewById<ImageView>(R.id.dialogItemImage)
        val name = view.findViewById<TextView>(R.id.dialogItemName)
        val info = view.findViewById<TextView>(R.id.dialogItemInfo)
        val closeButton = view.findViewById<Button>(R.id.closeButton)

        name.text = itemCounter.name

        Glide.with(requireContext())
            .load(itemCounter.iconUrl)
            .placeholder(R.drawable.item_not_found)
            .error(R.drawable.item_not_found)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(image)

        info.text = buildString {
            appendLine("Amount owned: ${itemCounter.quantity}")
            appendLine("Value: will be added soon")
            appendLine("Rarity: will be added soon")
        }

        closeButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}