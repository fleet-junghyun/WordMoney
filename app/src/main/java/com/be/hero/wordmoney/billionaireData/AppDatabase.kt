package com.be.hero.wordmoney.billionaireData

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.be.hero.wordmoney.quoteData.QuoteDao
import com.be.hero.wordmoney.quoteData.QuoteEntity

@Database(entities = [BillionaireEntity::class, QuoteEntity::class], version = 1, exportSchema = false)
@TypeConverters(StringListConverter::class)

abstract class AppDatabase : RoomDatabase() {
    abstract fun billionaireDao(): BillionaireDao
    abstract fun quoteDao(): QuoteDao // ✅ 명언(Quote) DAO 추가

    companion object {
        private var INSTANCE: AppDatabase? = null
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "word_money_database"
            )
                .build()
        }

        fun get(context: Context) = INSTANCE ?: synchronized(this) {
            buildDatabase(context).also {
                INSTANCE = it
            }
        }

//        private var INSTANCE: AppDatabase? = null
//        fun getDatabase(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    "word_money_database"
//                ).build()
//
//                fun get(context: Context) = INSTANCE ?: synchronized(this){
//                    getDatabase(context).also { INSTANCE = it }
//                }
//
//                INSTANCE = instance
//                instance
//            }
//        }


    }
}

