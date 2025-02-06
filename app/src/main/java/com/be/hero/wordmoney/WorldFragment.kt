package com.be.hero.wordmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.be.hero.wordmoney.adapter.BillionaireAdapter
import com.be.hero.wordmoney.adapter.VerticalSpaceItemDecoration
import com.be.hero.wordmoney.billionaireData.AppDatabase
import com.be.hero.wordmoney.billionaireData.BillionaireEntity
import com.be.hero.wordmoney.billionaireData.BillionaireViewModel
import com.be.hero.wordmoney.data.Billionaire
import com.be.hero.wordmoney.databinding.FragmentWorldBinding
import com.be.hero.wordmoney.quoteData.QuoteRepository


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
    ): View? {
        _binding = FragmentWorldBinding.inflate(inflater, container, false)

        billionaireViewModel = ViewModelProvider(this)[BillionaireViewModel::class.java]

        billionaireViewModel.billionaires.observe(viewLifecycleOwner) { billionaireEntities ->
            val billionaireList = billionaireEntities.map { convertEntityToBillionaire(it) }
            billionaireAdapter.submitList(billionaireList)
        }

        binding.recyclerViewWorld.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = billionaireAdapter
        }

        return binding.root
    }

    private fun convertEntityToBillionaire(entity: BillionaireEntity): Billionaire {
        return Billionaire(
            id = entity.id,
            uuid = entity.uuid,
            name = entity.name,
            netWorth = entity.netWorth,
            description = entity.description,
            quoteCount = entity.quoteCount,
            isSelected = entity.isSelected,
            category = entity.category,
            listPosition = entity.listPosition
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}