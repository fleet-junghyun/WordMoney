package com.be.hero.wordmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.be.hero.wordmoney.billionaireAdapter.BillionaireAdapter
import com.be.hero.wordmoney.billionaireData.BillionaireViewModel
import com.be.hero.wordmoney.databinding.FragmentWorldBinding


class WorldFragment : Fragment() {
    private var _binding: FragmentWorldBinding? = null
    private val binding get() = _binding!!
    private lateinit var billionaireViewModel: BillionaireViewModel

    private val billionaireAdapter: BillionaireAdapter by lazy {
        BillionaireAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorldBinding.inflate(inflater, container, false)

        billionaireViewModel = ViewModelProvider(this)[BillionaireViewModel::class.java]

        billionaireViewModel.billionaires.observe(viewLifecycleOwner) { billionaire ->
            billionaireAdapter.submitList(billionaire)
        }
        binding.recyclerViewWorld.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = billionaireAdapter
        }

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}