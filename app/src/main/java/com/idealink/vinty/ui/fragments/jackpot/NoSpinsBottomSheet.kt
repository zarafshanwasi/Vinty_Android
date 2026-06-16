package com.idealink.vinty.ui.fragments.jackpot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.idealink.vinty.R

class NoSpinsBottomSheet(
    private val message: String,
    private val onOk: (() -> Unit)? = null
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_no_spins, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val messageText = view.findViewById<TextView>(R.id.messageText)
        val okButton = view.findViewById<MaterialButton>(R.id.okButton)

        messageText.text = message

        okButton.setOnClickListener {
            dismiss()
            onOk?.invoke()
        }
    }
}
