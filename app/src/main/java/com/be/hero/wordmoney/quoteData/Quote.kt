package com.be.hero.wordmoney.quoteData

data class Quote (
    val id: Int,               // 고유 ID (Firestore & Room에서 사용)
    val richId: Int,           // 해당 명언을 말한 부자의 ID
    val uuid: String,          // Firestore의 문서 UUID
    val quote: String,         // 명언 내용
    val author: String,        // 명언을 말한 부자 이름
    val isBookmarked: Boolean  // 사용자가 북마크한 여부
)