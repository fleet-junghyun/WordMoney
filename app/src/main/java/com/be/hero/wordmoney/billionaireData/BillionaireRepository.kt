package com.be.hero.wordmoney.billionaireData

import android.util.Log
import androidx.lifecycle.LiveData
import com.be.hero.wordmoney.data.Billionaire
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillionaireRepository(private val db: AppDatabase) {

    private val firestore = FirebaseFirestore.getInstance()

    private val billionaireDao = db.billionaireDao()


    suspend fun fetchAndSaveBillionairesToLocalIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            // 1️⃣ 로컬 DB에 저장된 부자들의 ID 목록 가져오기
            val localIds = db.billionaireDao().getAllBillionaireIds()

            firestore.collection("billionaires")
                .get()
                .addOnSuccessListener { documents ->
                    val newBillionaires = mutableListOf<BillionaireEntity>()

                    for (document in documents) {
                        val billionaire = convertDocumentToBillionaireEntity(document)

                        // 2️⃣ Firestore의 id가 로컬 DB에 없으면 새 데이터로 추가
                        if (!localIds.contains(billionaire.id)) {
                            newBillionaires.add(billionaire)
                        }
                    }

                    // 3️⃣ 새 데이터가 있다면 Room에 삽입
                    if (newBillionaires.isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            db.billionaireDao().insertBillionaires(newBillionaires)
                            Log.d("Firestore", "🔥 Firestore에서 새 데이터를 가져와 Room에 저장 완료!")
                        }
                    } else {
                        Log.d("Firestore", "✅ 새로운 데이터 없음. Room 업데이트 필요 없음.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "❌ Firestore 데이터 가져오기 실패: ${e.message}")
                }
        }
    }


    // ✅ Room의 LiveData를 직접 반환하도록 수정
     fun getAllBillionaires(): LiveData<List<BillionaireEntity>> {
        return billionaireDao.getAllBillionaires()
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

    private fun convertDocumentToBillionaireEntity(document: DocumentSnapshot): BillionaireEntity {
        return BillionaireEntity(
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
    }

    // ✅ 특정 부자의 isSelected 값을 업데이트하는 함수
    suspend fun updateBillionaireSelection(billionaireId: Int, isSelected: Boolean) {
        billionaireDao.updateBillionaireSelection(billionaireId, isSelected)
    }


}
