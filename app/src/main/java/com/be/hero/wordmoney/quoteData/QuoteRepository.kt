package com.be.hero.wordmoney.quoteData

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.be.hero.wordmoney.billionaireData.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuoteRepository(application: Application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val db = AppDatabase.get(application)
    private val quoteDao = db.quoteDao()

    // ✅ Room의 LiveData를 직접 반환하도록 수정
    fun getAllQuotes(): LiveData<List<Quote>> {
        return quoteDao.getAllQuotes()
    }

    /**
     * Firestore에서 해당 부자의 Quote 데이터를 가져와서 Room에 저장하는 함수
     * Document ID는 부자의 uuid로 Firestore에서 관리되고, 데이터는 quotes 필드에 배열로 저장되어 있다고 가정함.
     */
    fun fetchAndSaveQuotesByBillionaire(richId: Int, richUuid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            // 기존 Room 데이터의 Quote ID 리스트를 가져옴
            val localQuoteIds = quoteDao.getQuotesByBillionaireList(richId)

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
                            quoteDao.insertQuotes(newQuotes)
                            Log.d("QuoteRepository", "Firestore에서 새 명언 ${newQuotes.size}개 Room에 저장 완료!")
                        }
                    } else {
                        Log.d("QuoteRepository", "새로운 명언 없음.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("QuoteRepository", "Firestore 명언 데이터 가져오기 실패: ${e.message}")
                }
        }
    }
    // 명언 삭제 코드(rich_id)
    fun deleteQuotesByRichId(richId: Int){
        quoteDao.deleteQuotesByRichId(richId)
    }

    fun getRandomQuote(): Quote{
        return quoteDao.getRandomQuote()
    }



    companion object {
        private var INSTANCE: QuoteRepository? = null
        fun get(context: Context) = get(context.applicationContext as Application)
        fun get(application: Application) = INSTANCE ?: synchronized(this) {
            QuoteRepository(application).also {
                INSTANCE = it
            }
        }
    }
}