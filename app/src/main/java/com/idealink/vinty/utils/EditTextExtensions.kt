package com.idealink.vinty.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.idealink.vinty.R

fun Fragment.applyTypingFont(editText: EditText) {
    val medium = ResourcesCompat.getFont(requireContext(), R.font.poppins_medium)
    val semiBold = ResourcesCompat.getFont(requireContext(), R.font.poppins_semibold)

    editText.typeface = medium

    editText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            editText.typeface = if (s.isNullOrEmpty()) medium else semiBold
        }

        override fun afterTextChanged(s: Editable?) {}
    })
}

fun Fragment.refreshTypingFont(editText: EditText) {
    val font = ResourcesCompat.getFont(
        requireContext(),
        if (editText.text.isNullOrEmpty()) R.font.poppins_medium else R.font.poppins_semibold
    )
    editText.typeface = font
}
