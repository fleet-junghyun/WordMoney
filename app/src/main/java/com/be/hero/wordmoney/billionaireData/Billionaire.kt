package com.be.hero.wordmoney.billionaireData

data class Billionaire(
    val id: Int, // Firestore의 숫자형 ID (Room에서도 기본 키로 사용)
    val uuid: String,        // Firestore 문서의 UUID
    val name: String,        // 부자 이름
    val netWorth: String,    // 자산 정보
    val description: List<String>, // Firestore에서 가져온 description 리스트
    val quoteCount: Int,     // 명언 수
    val isSelected: Boolean, // 사용자가 선택한 여부
    val category: Int,       // 카테고리 정보 (예: 세계, 한국, 일본 등)
    val listPosition: Int    // 리스트 내 노출 순서
)
