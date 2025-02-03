package com.be.hero.wordmoney.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.be.hero.wordmoney.R
import com.be.hero.wordmoney.data.Billionaire


class BillionaireAdapter(private val billionaireList: List<Billionaire>) :
    RecyclerView.Adapter<BillionaireAdapter.BillionaireViewHolder>() {

    // ViewHolder 정의
    class BillionaireViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rank:TextView = itemView.findViewById(R.id.tv_rank)
        val nameTextView: TextView = itemView.findViewById(R.id.rich_name)
        val propertyTextView: TextView = itemView.findViewById(R.id.property)
        val profileRecyclerView: RecyclerView = itemView.findViewById(R.id.rv_profile)
        val quoteCount:TextView = itemView.findViewById(R.id.tv_number_quotes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillionaireViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rich, parent, false)
        return BillionaireViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillionaireViewHolder, position: Int) {
        val billionaire = billionaireList[position]
        holder.nameTextView.text = billionaire.name
        holder.propertyTextView.text = billionaire.netWorth
        holder.rank.text = "${billionaire.listPosition}위"
        holder.quoteCount.text = "돈이 되는 말 총 ${billionaire.quoteCount}개"

        val profileModels = billionaire.description.map { ProfileModel(it) }

        // 내부 RecyclerView 설정
        holder.profileRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.profileRecyclerView.adapter = ProfileAdapter(profileModels)

        // 가로 간격 추가
        if (holder.profileRecyclerView.itemDecorationCount == 0) {
            val spaceInPx = (16 * holder.itemView.resources.displayMetrics.density).toInt() // 16dp를 px로 변환
            holder.profileRecyclerView.addItemDecoration(HorizontalSpaceItemDecoration(spaceInPx))
        }
    }

    override fun getItemCount(): Int = billionaireList.size
}