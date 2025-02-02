package com.be.hero.wordmoney.billionaireData

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BillionaireViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = BillionaireRepository(db)

    fun fetchAndSaveBillionaires() {
        viewModelScope.launch {
            repository.fetchAndSaveBillionairesToLocalIfNeeded()
        }
    }
}
