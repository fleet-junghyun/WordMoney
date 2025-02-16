package com.be.hero.wordmoney.billionaireData

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BillionaireDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillionaires(billionaireEntities: List<BillionaireEntity>)

    @Query("SELECT * FROM billionaires ORDER BY listPosition ASC")
    fun getAllBillionaires(): LiveData<List<Billionaire>>

    @Query("DELETE FROM billionaires")
    suspend fun deleteAllBillionaires()

    @Query("SELECT id FROM billionaires")
    fun getAllBillionaireIds(): List<Int> // Room에서 ID 리스트만 가져오기

    @Query("UPDATE billionaires SET isSelected = :isSelected WHERE id = :billionaireId")
    fun updateBillionaireSelection(billionaireId: Int, isSelected: Boolean) // ✅ 선택 상태 업데이트 추가

    @Query("SELECT COUNT(*) FROM billionaires WHERE isSelected = 1")
    fun getSelectedBillionaireCount(): Int
}