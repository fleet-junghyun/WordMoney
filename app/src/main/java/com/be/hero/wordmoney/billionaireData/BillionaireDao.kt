package com.be.hero.wordmoney.billionaireData

import androidx.room.*

@Dao
interface BillionaireDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillionaires(billionaires: List<Billionaire>)

    @Query("SELECT * FROM billionaires ORDER BY listPosition ASC")
    suspend fun getAllBillionaires(): List<Billionaire>

    @Query("DELETE FROM billionaires")
    suspend fun deleteAllBillionaires()
}