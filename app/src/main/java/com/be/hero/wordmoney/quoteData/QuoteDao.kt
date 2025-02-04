package com.be.hero.wordmoney.quoteData

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<QuoteEntity>) // 여러 개 명언 저장

    @Query("SELECT * FROM quotes WHERE richId = :richId ORDER BY id ASC")
    fun getQuotesByBillionaire(richId: Int): LiveData<List<QuoteEntity>> // 특정 부자의 명언 가져오기

    @Query("SELECT * FROM quotes WHERE isBookmarked = 1")
    fun getBookmarkedQuotes(): LiveData<List<QuoteEntity>> // 북마크된 명언만 가져오기

    @Query("UPDATE quotes SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmarkStatus(id: Int, isBookmarked: Boolean) // 북마크 상태 변경

    @Query("DELETE FROM quotes")
    suspend fun deleteAllQuotes() // 전체 명언 삭제

    @Query("SELECT id FROM quotes")
    fun getAllQuoteIds(): List<Int> // Room에 저장된 명언 ID 리스트 가져오기 (Firestore 비교용)

    @Query("SELECT id FROM quotes WHERE richId = :richId")
    fun getQuotesByBillionaireList(richId: Int): List<Int> // 🔥 특정 부자의 명언 ID 리스트 가져오기

    @Query("SELECT * FROM quotes ORDER BY id ASC")
    fun getAllQuotes(): LiveData<List<QuoteEntity>> // 🔥 모든 명언 가져오기
}