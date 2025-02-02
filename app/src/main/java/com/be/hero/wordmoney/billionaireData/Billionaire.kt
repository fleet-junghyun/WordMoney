package com.be.hero.wordmoney.billionaireData

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "billionaires")
data class Billionaire(
    @PrimaryKey val id: Int, // Firestore의 id 사용
    val rank : String,
    val name: String,
    val netWorth: String,
    @TypeConverters(StringListConverter::class) // 리스트 변환을 위해 추가
    val description: List<String>,
    val quoteCount: Int,
    val isSelected: Boolean,
    val category: Int,
    val listPosition: Int
)
