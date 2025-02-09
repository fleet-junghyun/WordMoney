package com.be.hero.wordmoney.quoteData

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.be.hero.wordmoney.data.Billionaire

class QuoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = QuoteRepository.get(application)
    val quotes: LiveData<List<Quote>> = repository.getAllQuotes()

    fun fetchAndSaveQuotesByBillionaire(billionaire: Billionaire) {
        repository.fetchAndSaveQuotesByBillionaire(billionaire.id, billionaire.uuid)
    }
}