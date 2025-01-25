package com.be.hero.wordmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.be.hero.wordmoney.adapter.Billionaire
import com.be.hero.wordmoney.adapter.BillionaireAdapter
import com.be.hero.wordmoney.adapter.ProfileModel
import com.be.hero.wordmoney.adapter.VerticalSpaceItemDecoration

class WorldFragment : Fragment() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BillionaireAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_world, container, false)

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.recycler_view_world)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 더미 데이터 설정
        val billionaireList = listOf(
            Billionaire("1위","일론 머스크", "320조원", listOf(ProfileModel("tesla"),ProfileModel("2"),ProfileModel("3"),ProfileModel("3"),ProfileModel("3"),ProfileModel("3"),ProfileModel("3"),) ),
            Billionaire("2위","제프 베조스", "300조원",listOf(ProfileModel("amazon"),ProfileModel("2"),ProfileModel("3"),ProfileModel("3"),ProfileModel("3"),ProfileModel("3"),ProfileModel("3"),) ),
            Billionaire("3위","빌 게이츠", "250조원",listOf(ProfileModel("microsoft"),ProfileModel("2"),ProfileModel("3"),ProfileModel("3"),ProfileModel("3"),ProfileModel("3"),ProfileModel("3"),ProfileModel("3"),) ),
            Billionaire("4위","워렌 버핏", "240조원",listOf(ProfileModel("주식 부자"),ProfileModel("2"),ProfileModel("3"),ProfileModel("3"),ProfileModel("3"),ProfileModel("3"),ProfileModel("3"),ProfileModel("3"),) )
        )

        // 어댑터 설정
        adapter = BillionaireAdapter(billionaireList)
        recyclerView.adapter = adapter
        val spaceInPx = (42 * resources.displayMetrics.density).toInt()
        recyclerView.addItemDecoration(VerticalSpaceItemDecoration(spaceInPx))
        return view
    }
}