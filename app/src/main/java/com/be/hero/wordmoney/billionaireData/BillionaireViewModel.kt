package com.be.hero.wordmoney.billionaireData

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.be.hero.wordmoney.data.Billionaire
import kotlinx.coroutines.launch

class BillionaireViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = BillionaireRepository(db)

    private val _billionaires = MutableLiveData<List<BillionaireEntity>>()
    val billionaires: LiveData<List<BillionaireEntity>> get() = _billionaires

    init {
        fetchAndSaveBillionaires()
    }

    fun fetchAndSaveBillionaires() {
        viewModelScope.launch {
            repository.fetchAndSaveBillionairesToLocalIfNeeded()
            _billionaires.postValue(repository.getAllBillionaires())
        }
    }

    fun insertMultipleBillionairesToFirestore(billionaires: List<Billionaire>) {
        for (billionaire in billionaires) {
            repository.insertBillionaireToFirestore(billionaire)
        }
    }
}
