package com.be.hero.wordmoney.billionaireData

import android.util.Log
import com.be.hero.wordmoney.data.Billionaire
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
                    val billionaireEntityList = mutableListOf<BillionaireEntity>()
                    for (document in documents) {
                        val billionaireEntity = BillionaireEntity(
                            id = document.getLong("id")?.toInt() ?: 0,
                            uuid = document.getString("uuid") ?: "",
                            name = document.getString("name") ?: "",
                            netWorth = document.getString("netWorth") ?: "",
                            description = document.get("description") as? List<String> ?: emptyList(),
                            quoteCount = document.getLong("quoteCount")?.toInt() ?: 0,
                            isSelected = document.getBoolean("isSelected") ?: false,
                            category = document.getLong("category")?.toInt() ?: 0,
                            listPosition = document.getLong("listPosition")?.toInt() ?: 0
                        )
                        billionaireEntityList.add(billionaireEntity)
                    }
                    // Room Database에 저장
                    CoroutineScope(Dispatchers.IO).launch {
                        db.billionaireDao().insertBillionaires(billionaireEntityList)
                        Log.d("Firestore", "✅ Firestore 데이터가 로컬 DB에 저장 완료!")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "❌ Firestore에서 데이터를 가져오는 중 오류 발생: ", e)
                }
        }
    }

    suspend fun getAllBillionaires(): List<BillionaireEntity> {
        return db.billionaireDao().getAllBillionaires()
    }

    fun insertBillionaireToFirestore(billionaire: Billionaire) {
        val db = FirebaseFirestore.getInstance()

        // Firestore에 삽입할 데이터 맵
        val data = hashMapOf(
            "id" to billionaire.id,
            "uuid" to billionaire.uuid,
            "name" to billionaire.name,
            "netWorth" to billionaire.netWorth,
            "description" to billionaire.description,
            "quoteCount" to billionaire.quoteCount,
            "isSelected" to billionaire.isSelected,
            "category" to billionaire.category,
            "listPosition" to billionaire.listPosition
        )

        // Firestore 컬렉션 "billionaires"에 문서 추가 (문서 ID는 UUID 사용)
        db.collection("billionaires").document(billionaire.uuid)
            .set(data)
            .addOnSuccessListener {
                println("${billionaire.name} 데이터 삽입 성공")
            }
            .addOnFailureListener { e ->
                println("❌ 데이터 삽입 실패: ${e.message}")
            }
    }

}
