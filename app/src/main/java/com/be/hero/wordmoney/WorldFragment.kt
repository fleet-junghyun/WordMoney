package com.be.hero.wordmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.be.hero.wordmoney.billionaireAdapter.BillionaireAdapter
import com.be.hero.wordmoney.billionaireData.BillionaireViewModel
import com.be.hero.wordmoney.billionaireData.Billionaire
import com.be.hero.wordmoney.databinding.FragmentWorldBinding
import com.be.hero.wordmoney.dialog.PremiumDialog
import com.be.hero.wordmoney.quoteData.QuoteViewModel
import kotlinx.coroutines.launch


class WorldFragment : Fragment() {
    private var _binding: FragmentWorldBinding? = null
    private val binding get() = _binding!!
    private lateinit var billionaireViewModel: BillionaireViewModel
    private lateinit var quoteViewModel: QuoteViewModel

    private val billionaireAdapter: BillionaireAdapter by lazy {
        BillionaireAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorldBinding.inflate(inflater, container, false)

        billionaireViewModel = ViewModelProvider(this)[BillionaireViewModel::class.java]
        quoteViewModel = ViewModelProvider(this)[QuoteViewModel::class.java]

        billionaireViewModel.billionaires.observe(viewLifecycleOwner) { billionaire ->
            billionaireAdapter.submitList(billionaire)
        }

        binding.recyclerViewWorld.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = billionaireAdapter
            billionaireAdapter.setOnItemClickListener(object : BillionaireAdapter.ItemClickListener {
                override fun addClick(billionaire: Billionaire) {

                    lifecycleScope.launch {
                        val count = billionaireViewModel.getSelectedBillionaireCount() // ✅ 직접 개수 확인

                        if (!billionaire.isSelected) {
                            if (count >= 5) {
                                openPremiumDialog() // ✅ 다이얼로그 띄우기
                            } else {
                                quoteViewModel.fetchAndSaveQuotesByBillionaire(billionaire)
                                val updatedBillionaire = billionaire.copy(isSelected = !billionaire.isSelected)
                                billionaireViewModel.updateBillionaireIsSelected(updatedBillionaire)
                            }
                        } else {
                            //해당 quote 삭제 코드
                            quoteViewModel.deleteQuotesForBillionaire(billionaire.id)
                            val updatedBillionaire = billionaire.copy(isSelected = !billionaire.isSelected)
                            billionaireViewModel.updateBillionaireIsSelected(updatedBillionaire)                        }
                    }
                }
            })
        }

        return binding.root
    }

    private fun openPremiumDialog() {
        if (!isAdded) return
        val dialog = PremiumDialog()
        dialog.show(childFragmentManager, "premium_dialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}