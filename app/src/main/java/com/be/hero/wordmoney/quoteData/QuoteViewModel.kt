package com.be.hero.wordmoney.quoteData

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.be.hero.wordmoney.billionaireData.Billionaire
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = QuoteRepository.get(application)
    val quotes: LiveData<List<Quote>> = repository.getAllQuotes()

    private val _randomQuote = MutableLiveData<Quote?>()
    val randomQuote: LiveData<Quote?> get() = _randomQuote


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

    fun getRandomQuote() {
        viewModelScope.launch(Dispatchers.IO) {
            val quote = repository.getRandomQuote()
            _randomQuote.postValue(quote)
        }
    }

    // ✅ 위젯 및 Worker에서 사용할 즉시 데이터 반환 함수 추가
    suspend fun getRandomQuoteSync(): Quote? {
        return withContext(Dispatchers.IO) {
            repository.getRandomQuote()
        }
    }
}