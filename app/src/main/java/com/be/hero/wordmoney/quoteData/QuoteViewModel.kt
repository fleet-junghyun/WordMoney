package com.be.hero.wordmoney.quoteData

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.be.hero.wordmoney.billionaireData.Billionaire
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = QuoteRepository.get(application)
    val quotes: LiveData<List<Quote>> = repository.getAllQuotes()

    fun fetchAndSaveQuotesByBillionaire(billionaire: Billionaire) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.fetchAndSaveQuotesByBillionaire(billionaire.id, billionaire.uuid)
        }
    }

    fun deleteQuotesForBillionaire(richId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteQuotesByRichId(richId)
        }
    }
}