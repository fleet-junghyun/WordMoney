package com.be.hero.wordmoney.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.be.hero.wordmoney.data.Billionaire
import com.be.hero.wordmoney.databinding.ItemRichBinding


class BillionaireAdapter : ListAdapter<Billionaire, BillionaireAdapter.BillionaireViewHolder>(DIFF_CALLBACK){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillionaireViewHolder {
        val binding = ItemRichBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BillionaireViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillionaireViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

//    override fun onBindViewHolder(holder: BillionaireViewHolder, position: Int) {
////        val billionaire = billionaireList[position]
////        holder.nameTextView.text = billionaire.name
////        holder.propertyTextView.text = billionaire.netWorth
////        holder.rank.text = "${billionaire.listPosition}위"
////        holder.quoteCount.text = "돈이 되는 말 총 ${billionaire.quoteCount}개"
////        val isSelected = if (billionaire.isSelected) R.drawable.ic_is_selected else R.drawable.ic_add
////        holder.addIcon.setImageResource(isSelected)
////
////        val profileModels = billionaire.description.map { ProfileModel(it) }
////
////        // 내부 RecyclerView 설정
////        holder.profileRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
////        holder.profileRecyclerView.adapter = ProfileAdapter(profileModels)
////        // 가로 간격 추가
////        if (holder.profileRecyclerView.itemDecorationCount == 0) {
////            val spaceInPx = (16 * holder.itemView.resources.displayMetrics.density).toInt() // 16dp를 px로 변환
////            holder.profileRecyclerView.addItemDecoration(HorizontalSpaceItemDecoration(spaceInPx))
////        }
////
////        holder.btnAdd.setOnClickListener {
////            quoteRepository.fetchAndSaveQuotesByBillionaire(billionaire.id, billionaire.uuid)
////
////        }
//
//
//        holder.bind(getItem)
//    }

    class BillionaireViewHolder(private val binding: ItemRichBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(billionaire: Billionaire) {
            binding.tvRank.text = "${billionaire.listPosition}위"
            binding.richName.text = billionaire.name
            binding.property.text = billionaire.netWorth
            binding.tvNumberQuotes.text = "돈이 되는 말 총 ${billionaire.quoteCount}개"
        }
    }


//    inner class BillionaireViewHolder(porivate val binding : ItemRichBinding) : RecyclerView.ViewHolder(binding.root){
//        fun bind(data : Billionaire){
//
//        }
//    }

//    override fun getItemCount(): Int = billionaireList.size

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Billionaire>() {
            override fun areItemsTheSame(oldItem: Billionaire, newItem: Billionaire): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Billionaire, newItem: Billionaire): Boolean {
                return oldItem == newItem
            }
        }
    }
}