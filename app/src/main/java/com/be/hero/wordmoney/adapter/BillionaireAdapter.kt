package com.be.hero.wordmoney.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
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

    class BillionaireViewHolder(private val binding: ItemRichBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(billionaire: Billionaire) {
            binding.tvRank.text = "${billionaire.listPosition}위"
            binding.richName.text = billionaire.name
            binding.property.text = billionaire.netWorth
            binding.tvNumberQuotes.text = "돈이 되는 말 총 ${billionaire.quoteCount}개"

            val profileModels = billionaire.description.map { ProfileModel(it) }
            binding.rvProfile.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            binding.rvProfile.adapter = ProfileAdapter(profileModels)
            if (binding.rvProfile.itemDecorationCount == 0) {
                val spaceInPx = (16 * binding.root.resources.displayMetrics.density).toInt() // 16dp를 px로 변환
                binding.rvProfile.addItemDecoration(HorizontalSpaceItemDecoration(spaceInPx))
            }
        }
    }

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