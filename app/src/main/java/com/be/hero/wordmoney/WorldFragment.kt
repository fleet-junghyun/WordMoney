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
import com.be.hero.wordmoney.billionaireData.BillionaireEntity
import com.be.hero.wordmoney.billionaireData.BillionaireViewModel
import com.be.hero.wordmoney.data.Billionaire


class WorldFragment : Fragment() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BillionaireAdapter
    private val billionaireList = mutableListOf<BillionaireEntity>() // Room에서 가져올 데이터 저장
    private lateinit var billionaireViewModel: BillionaireViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_world, container, false)

        billionaireViewModel = ViewModelProvider(this)[BillionaireViewModel::class.java]

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.recycler_view_world)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = BillionaireAdapter(billionaireList.map { convertEntityToBillionaire(it) })
        recyclerView.adapter = adapter
        val spaceInPx = (42 * resources.displayMetrics.density).toInt()
        recyclerView.addItemDecoration(VerticalSpaceItemDecoration(spaceInPx))

        // Room에서 데이터 가져오기
        fetchBillionairesFromRoom()

        return view
    }

    private fun fetchBillionairesFromRoom() {
        billionaireViewModel.billionaires.observe(viewLifecycleOwner) { billionaireEntities ->
            billionaireList.clear()
            billionaireList.addAll(billionaireEntities)
            adapter = BillionaireAdapter(billionaireList.map { convertEntityToBillionaire(it) })
            recyclerView.adapter = adapter
        }
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
}