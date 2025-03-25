package com.be.hero.wordmoney.billionaireAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.be.hero.wordmoney.R
import com.be.hero.wordmoney.billionaireData.Billionaire
import com.be.hero.wordmoney.databinding.ItemRichBinding


class BillionaireAdapter : ListAdapter<Billionaire, BillionaireAdapter.BillionaireViewHolder>(DIFF_CALLBACK) {

    interface ItemClickListener {
        fun addClick(billionaire: Billionaire)
    }

    private var itemClickListener: ItemClickListener? = null

    fun setOnItemClickListener(listener: ItemClickListener) {
        this.itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillionaireViewHolder {
        val binding = ItemRichBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BillionaireViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillionaireViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BillionaireViewHolder(private val binding: ItemRichBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(billionaire: Billionaire) {
            binding.richName.text = billionaire.name
            binding.property.text = billionaire.netWorth

            val profileModels = billionaire.description.map { ProfileModel(it) }
            binding.rvProfile.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            binding.rvProfile.adapter = ProfileAdapter(profileModels)
            if (binding.rvProfile.itemDecorationCount == 0) {
                val spaceInPx = (16 * binding.root.resources.displayMetrics.density).toInt() // 16dp를 px로 변환
                binding.rvProfile.addItemDecoration(HorizontalSpaceItemDecoration(spaceInPx))
            }
            // ✅ `isSelected` 값에 따라 아이콘 변경
            binding.ivUnselected.setImageResource(
                if (billionaire.isSelected) R.drawable.ic_is_selected // ✅ 선택된 상태 아이콘
                else R.drawable.ic_unselected // ✅ 선택되지 않은 상태 아이콘
            )

            binding.btnAdd.setOnClickListener {
                itemClickListener?.addClick(billionaire)
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