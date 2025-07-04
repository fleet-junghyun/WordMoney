package com.be.hero.wordmoney.quoteAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.be.hero.wordmoney.R
import com.be.hero.wordmoney.billionaireData.Billionaire
import com.be.hero.wordmoney.quoteData.Quote
import com.be.hero.wordmoney.quoteData.QuoteEntity

class QuotePagerAdapter(private var quotes: List<Quote>) : RecyclerView.Adapter<QuotePagerAdapter.QuoteViewHolder>() {

    interface ShareClickListener{
        fun shareClick(quote: Quote)
    }

    private var shareClickListener : ShareClickListener? = null

    fun setShareClickListener(listener : ShareClickListener){
        this.shareClickListener = listener
    }

    class QuoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val quoteTextView: TextView = itemView.findViewById(R.id.tv_quote)
        val authorTextView: TextView = itemView.findViewById(R.id.tv_author)
        val share : ImageView = itemView.findViewById(R.id.share)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quote, parent, false)
        return QuoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val quote = quotes[position]
        holder.quoteTextView.text = quote.quote
        holder.authorTextView.text = "- ${quote.author}"


        // 공유 버튼 클릭 이벤트 추가
        holder.share.setOnClickListener {
            shareClickListener?.shareClick(quote) // 예시로 Billionaire 객체 생성
        }

    }

    override fun getItemCount(): Int = quotes.size

    // 🔥 데이터 변경 시 갱신
    fun updateQuotes(newQuotes: List<Quote>) {
        quotes = newQuotes
        notifyDataSetChanged()
    }
}