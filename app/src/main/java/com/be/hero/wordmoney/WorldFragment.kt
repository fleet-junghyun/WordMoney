package com.be.hero.wordmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.be.hero.wordmoney.billionaireAdapter.BillionaireAdapter
import com.be.hero.wordmoney.billionaireData.BillionaireViewModel
import com.be.hero.wordmoney.billionaireData.Billionaire
import com.be.hero.wordmoney.databinding.FragmentWorldBinding
import com.be.hero.wordmoney.quoteData.QuoteViewModel


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
                    if (!billionaire.isSelected) {
                        quoteViewModel.fetchAndSaveQuotesByBillionaire(billionaire)
                    } else {
                        //해당 quote 삭제 코드
                        Toast.makeText(context, billionaire.id.toString(), Toast.LENGTH_SHORT).show()
                        quoteViewModel.deleteQuotesForBillionaire(billionaire.id)
                    }
                    val updatedBillionaire = billionaire.copy(isSelected = !billionaire.isSelected)
                    Toast.makeText(context, updatedBillionaire.isSelected.toString(), Toast.LENGTH_SHORT).show()
                    billionaireViewModel.updateBillionaireIsSelected(updatedBillionaire)
                }
            })
        }

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}