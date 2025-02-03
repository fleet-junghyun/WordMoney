package com.be.hero.wordmoney.billionaireData

import androidx.room.*

@Dao
interface BillionaireDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillionaires(billionaireEntities: List<BillionaireEntity>)

    @Query("SELECT * FROM billionaires ORDER BY listPosition ASC")
    suspend fun getAllBillionaires(): List<BillionaireEntity>

    @Query("DELETE FROM billionaires")
    suspend fun deleteAllBillionaires()

    @Query("SELECT id FROM billionaires")
    fun getAllBillionaireIds(): List<Int> // Room에서 ID 리스트만 가져오기


}