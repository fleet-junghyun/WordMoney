package com.be.hero.wordmoney.billionaireData

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillionaireViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BillionaireRepository.get(application)

    // ✅ MutableLiveData 대신 Room의 LiveData 직접 사용
    val billionaires: LiveData<List<Billionaire>> = repository.getAllBillionaires()

    init {
        firstSaveBillionaires()
    }

    private fun firstSaveBillionaires() {
        viewModelScope.launch {
            repository.SaveBillionairesToRoomFromFirestore()
        }
    }

    fun updateBillionaireIsSelected(billionaire: Billionaire) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBillionaireIsSelected(billionaire)
        }
    }

    fun insertMultipleBillionairesToFirestore(billionaires: List<Billionaire>) {
        for (billionaire in billionaires) {
            repository.insertBillionaireToFirestore(billionaire)
        }
    }

}
