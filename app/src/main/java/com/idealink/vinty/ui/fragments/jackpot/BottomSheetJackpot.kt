package com.idealink.vinty.ui.fragments.jackpot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.idealink.vinty.R
import com.idealink.vinty.databinding.FragmentBottomSheetJackpotBinding

class BottomSheetJackpot : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetJackpotBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetJackpotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        binding.spinWheelBtn.setOnClickListener {
            dismiss()
            findNavController().navigate(
                R.id.action_homeFragment_to_fragmentJackpot
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
