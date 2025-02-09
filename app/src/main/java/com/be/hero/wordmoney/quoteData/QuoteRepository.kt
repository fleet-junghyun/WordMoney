package com.be.hero.wordmoney.quoteData

import android.util.Log
import com.be.hero.wordmoney.billionaireData.AppDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class QuoteRepository(private val db: AppDatabase) {
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Firestore에서 해당 부자의 Quote 데이터를 가져와서 Room에 저장하는 함수
     * Document ID는 부자의 uuid로 Firestore에서 관리되고, 데이터는 quotes 필드에 배열로 저장되어 있다고 가정함.
     */
    fun fetchAndSaveQuotesByBillionaire(richId: Int, richUuid: String, callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            // 기존 Room 데이터의 Quote ID 리스트를 가져옴
            val localQuoteIds = db.quoteDao().getQuotesByBillionaireList(richId)

            firestore.collection("quotes").document(richUuid)
                .get()
                .addOnSuccessListener { document ->
                    // Firestore 문서에서 "quotes" 배열을 가져옴 (각 항목은 Map<String, Any> 형태)
                    val quotesArray = document["quotes"] as? List<Map<String, Any>> ?: emptyList()
                    val newQuotes = mutableListOf<QuoteEntity>()

                    for (quoteData in quotesArray) {
                        // QuoteEntity와 Quote data class가 동일한 구조이므로 그대로 생성
                        val quote = QuoteEntity(
                            id = (quoteData["id"] as? Long)?.toInt() ?: 0,
                            richId = (quoteData["richId"] as? Long)?.toInt() ?: 0,
                            uuid = quoteData["uuid"] as? String ?: "",
                            quote = quoteData["quote"] as? String ?: "",
                            author = quoteData["author"] as? String ?: "",
                            isBookmarked = quoteData["isBookmarked"] as? Boolean ?: false
                        )

                        if (!localQuoteIds.contains(quote.id)) {
                            newQuotes.add(quote)
                        }
                    }

                    if (newQuotes.isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            db.quoteDao().insertQuotes(newQuotes)
                            Log.d("QuoteRepository", "Firestore에서 새 명언 ${newQuotes.size}개 Room에 저장 완료!")
                            callback(true)
                        }
                    } else {
                        Log.d("QuoteRepository", "새로운 명언 없음.")
                        callback(true)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("QuoteRepository", "Firestore 명언 데이터 가져오기 실패: ${e.message}")
                    callback(false)
                }
        }
    }
}