package com.be.hero.wordmoney.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.be.hero.wordmoney.R

data class ProfileModel(
    val profile: String,  // 내부 RecyclerView 아이템 이름
)

class ProfileAdapter(private val items: List<ProfileModel>) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profile: TextView = itemView.findViewById(R.id.tv_profile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val item = items[position]
        holder.profile.text = item.profile
    }

    override fun getItemCount(): Int = items.size
}