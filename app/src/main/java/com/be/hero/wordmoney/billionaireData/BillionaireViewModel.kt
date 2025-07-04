package com.be.hero.wordmoney.billionaireData

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillionaireViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BillionaireRepository.get(application)

    val sortedBillionaires: LiveData<List<Billionaire>> = repository.getAllBillionaires().map { list ->
        list.sortedByDescending { it.property }
    }

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

    // Selected = true인 것만 갖고오기.
    suspend fun getSelectedBillionaireCount(): Int {
        return withContext(Dispatchers.IO) { // ✅ 백그라운드에서 실행
            repository.getSelectedBillionaireCount()
        }
    }

    fun insertMultipleBillionairesToFirestore(billionaires: List<Billionaire>) {
        for (billionaire in billionaires) {
            repository.insertBillionaireToFirestore(billionaire)
        }
    }

}
