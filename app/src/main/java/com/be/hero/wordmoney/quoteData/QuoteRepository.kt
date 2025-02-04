package com.be.hero.wordmoney.quoteData

import android.util.Log
import com.be.hero.wordmoney.billionaireData.AppDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuoteRepository(private val db: AppDatabase) {
    private val firestore = FirebaseFirestore.getInstance()

    // 특정 부자의 명언을 Firestore에서 가져와 Room에 저장
    fun fetchAndSaveQuotesByBillionaire(richId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val localQuoteIds = db.quoteDao().getQuotesByBillionaireList(richId) // Room에 저장된 명언 목록 가져오기

            firestore.collection("quotes").document("c30f4a76-307c-4bb3-aba6-e48c75cbe363") // 🔴 Document ID는 동적으로 변경 가능
                .get()
                .addOnSuccessListener { document ->
                    val quotesArray = document["quotes"] as? List<Map<String, Any>> ?: emptyList()
                    val newQuotes = mutableListOf<QuoteEntity>()

                    for (quoteData in quotesArray) {
                        val quote = convertMapToQuoteEntity(quoteData)
                        // 🔥 기존 ID 리스트와 비교하여 새로운 데이터만 추가
                        if (!localQuoteIds.contains(quote.id)) {
                            newQuotes.add(quote)
                        }    }

                    if (newQuotes.isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            db.quoteDao().insertQuotes(newQuotes)
                            Log.d("Firestore", "🔥 Firestore에서 부자 명언을 Room에 저장 완료!")
                        }
                    } else {
                        Log.d("Firestore", "✅ 새로운 명언 없음. Room 업데이트 필요 없음.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "❌ Firestore 명언 데이터 가져오기 실패: ${e.message}")
                }
        }
    }



    private fun convertMapToQuoteEntity(data: Map<String, Any>): QuoteEntity {
        return QuoteEntity(
            id = (data["id"] as? Long)?.toInt() ?: 0,
            richId = (data["richId"] as? Long)?.toInt() ?: 0,
            uuid = data["uuid"] as? String ?: "",
            quote = data["quote"] as? String ?: "",
            author = data["author"] as? String ?: "",
            isBookmarked = data["isBookmarked"] as? Boolean ?: false
        )
    }
}