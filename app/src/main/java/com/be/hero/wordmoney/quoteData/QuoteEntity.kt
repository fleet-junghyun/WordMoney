package com.be.hero.wordmoney.quoteData

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey (autoGenerate = true)
    val id: Int = 0,  // Room에서 기본 키
    val richId: Int,          // 해당 명언을 말한 부자의 ID
    val uuid: String,         // Firestore의 문서 UUID
    val quote: String,        // 명언 내용
    val author: String,       // 명언을 말한 사람
    val isBookmarked: Boolean // 북마크 여부
)