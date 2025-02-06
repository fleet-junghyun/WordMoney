package com.be.hero.wordmoney.billionaireData

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.be.hero.wordmoney.data.Billionaire
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillionaireViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = BillionaireRepository(db)

    // ✅ MutableLiveData 대신 Room의 LiveData 직접 사용
    val billionaires: LiveData<List<BillionaireEntity>> = repository.getAllBillionaires()

    init {
        fetchAndSaveBillionaires()
    }

     private fun fetchAndSaveBillionaires() {
        viewModelScope.launch {
            repository.fetchAndSaveBillionairesToLocalIfNeeded()
        }
    }

    fun insertMultipleBillionairesToFirestore(billionaires: List<Billionaire>) {
        for (billionaire in billionaires) {
            repository.insertBillionaireToFirestore(billionaire)
        }
    }

    // ✅ isSelected 상태 변경 함수
    fun updateBillionaireSelection(billionaireId: Int, isSelected: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBillionaireSelection(billionaireId, isSelected)
        }
    }
}
