package com.be.hero.wordmoney.billionaireData

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillionaireRepository(private val db: AppDatabase) {
    private val firestore = FirebaseFirestore.getInstance()

    fun fetchAndSaveBillionairesToLocalIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            val localData = db.billionaireDao().getAllBillionaires()
            if (localData.isNotEmpty()) {
                Log.d("Firestore", "✅ 로컬 DB에 데이터가 이미 존재합니다. Firestore에서 가져오지 않음.")
                return@launch
            }

            Log.d("Firestore", "📥 Firestore에서 데이터 가져오는 중...")

            firestore.collection("billionaires")
                .get()
                .addOnSuccessListener { documents ->
                    val billionaireList = mutableListOf<Billionaire>()

                    for (document in documents) {
                        val billionaire = Billionaire(
                            id = document.getLong("id")?.toInt() ?: 0,
                            name = document.getString("name") ?: "",
                            netWorth = document.getString("netWorth") ?: "",
                            description = document.get("description") as? List<String> ?: emptyList(),
                            quoteCount = document.getLong("quoteCount")?.toInt() ?: 0,
                            isSelected = document.getBoolean("isSelected") ?: false,
                            category = document.getLong("category")?.toInt() ?: 0,
                            listPosition = document.getLong("listPosition")?.toInt() ?: 0
                        )
                        billionaireList.add(billionaire)
                    }

                    // Room Database에 저장
                    CoroutineScope(Dispatchers.IO).launch {
                        db.billionaireDao().insertBillionaires(billionaireList)
                        Log.d("Firestore", "✅ Firestore 데이터가 로컬 DB에 저장 완료!")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "❌ Firestore에서 데이터를 가져오는 중 오류 발생: ", e)
                }
        }
    }
}
