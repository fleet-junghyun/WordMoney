package com.be.hero.wordmoney.dialog

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.be.hero.wordmoney.databinding.DialogPremiumBinding

class PremiumDialog : CompatBottomSheetDialog() {

    private var _binding: DialogPremiumBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPremiumBinding.inflate(inflater, container, false)

        binding.apply {
            exit.setOnClickListener { dismiss() }
        }

        return binding.root
    }
}