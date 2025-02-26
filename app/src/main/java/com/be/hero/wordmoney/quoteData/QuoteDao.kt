package com.be.hero.wordmoney.quoteData

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuoteDao {

    @Query("SELECT * FROM quotes ORDER BY RANDOM()")
    fun getAllQuotes(): LiveData<List<Quote>> // 🔥 모든 명언 가져오기

    @Insert
    suspend fun insertQuotes(quotes: List<QuoteEntity>) // 여러 개 명언 저장

    @Query("SELECT id FROM quotes")
    fun getAllQuoteIds(): List<Int> // Room에 저장된 명언 ID 리스트 가져오기 (Firestore 비교용)

    @Query("SELECT id FROM quotes WHERE richId = :richId")
    fun getQuotesByBillionaireList(richId: Int): List<Int> // 🔥 특정 부자의 명언 ID 리스트 가져오기


    @Query("DELETE FROM quotes WHERE richId = :richId")
    fun deleteQuotesByRichId(richId: Int)

    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT 1")
    fun getRandomQuote() : Quote

}