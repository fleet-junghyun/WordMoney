package com.be.hero.wordmoney.billionaireData

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BillionaireViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillionaireViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BillionaireViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
