package com.be.hero.wordmoney.billionaireData

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "billionaires")
data class BillionaireEntity(
    @PrimaryKey val id: Int, // Firestore의 id 사용
    val uuid: String,
    val name: String,
    val netWorth: String,
    val property:Long, //자산 숫자.
    @TypeConverters(StringListConverter::class) // 리스트 변환을 위해 추가
    val description: List<String>,
    val isSelected: Boolean,
    val category: Int,
    val listPosition: Int
)
