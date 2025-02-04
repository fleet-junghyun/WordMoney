package com.be.hero.wordmoney.quoteData

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.be.hero.wordmoney.billionaireData.AppDatabase
import kotlinx.coroutines.launch

class QuoteViewModel(application: Application) : AndroidViewModel(application) {
    private val quoteDao = AppDatabase.getDatabase(application).quoteDao()
    val allQuotes: LiveData<List<QuoteEntity>> = quoteDao.getAllQuotes() // 모든 명언 가져오기

    fun fetchQuotes() {
        viewModelScope.launch {
            quoteDao.getAllQuotes() // Room에서 모든 명언 가져오기
        }
    }

}